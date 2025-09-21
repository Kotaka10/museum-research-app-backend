package com.example.museumsearch.controller;

import java.util.List;

import org.springframework.security.core.AuthenticationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.NoSuchElementException;

import com.example.museumsearch.dto.ChangePasswordRequest;
import com.example.museumsearch.dto.LoginResponse;
import com.example.museumsearch.dto.UserDTO;
import com.example.museumsearch.dto.ViewedMuseumRequest;
import com.example.museumsearch.dto.ViewedMuseumResponse;
import com.example.museumsearch.model.User;
import com.example.museumsearch.repository.UserRepository;
import com.example.museumsearch.security.JwtProvider;
import com.example.museumsearch.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtProvider jwtProvider;

    public static class RegisterRequest {
        @Email(message = "有効なメールアドレスを入力してください")
        @NotBlank(message = "メールアドレスは必須です") 
        public String email;

        @NotBlank(message = "パスワードは必須です")
        @Size(min = 6, message = "パスワードは6文字以上で入力してください")
        public String password;

        @NotBlank(message = "表示名は必須です")
        public String userName;
    }

    public static class LoginRequest {
        private String userName;
        private String password;

        public LoginRequest() {}

        public String getUserName() {
            return userName;
        }

        public String getPassword() {
            return password;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        try {
            userService.registerUser(request.email, request.password, request.userName);

            String token = userService.login(request.email, request.password);

            User user = userService.findUserByEmail(request.email);
            UserDTO userDTO = new UserDTO(user.getId(), user.getUserName());

            ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(86400)
                .build();

            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new LoginResponse(token, userDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("登録エラー: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            String userName = request.getUserName();

            User user = userRepository.findByUserName(userName)
                    .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりませんでした"));

            String token = jwtProvider.generateToken(user.getUserName(), List.of("ROLE_" + user.getRoles()));

            ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(true) 
                .path("/")
                .sameSite("None")
                .maxAge(86400)
                .build();

            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new LoginResponse(token, new UserDTO(user.getId(), user.getUserName())));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("ログイン失敗:" + e.getMessage()); 
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/mypage")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userName = principal.getUsername();
        User user = userService.findUserByUserName(userName);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        log.info("認証ユーザー: {}", principal);
        String userName = principal.getUsername();
        userService.deleteUserByUserName(userName);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> editUser(@Valid @PathVariable Long id, @RequestBody User user) {
        log.info("ユーザー情報を編集します: id={}, 新しい内容={}", id, user);
        return ResponseEntity.ok(userService.editUser(id, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("ユーザーを削除します: id={}", id);
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> findUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findUserById(id));
    }

    @GetMapping
    public ResponseEntity<User> findUserByEmail(String email) {
        return ResponseEntity.ok(userService.findUserByEmail(email));
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsUserByEamil(@Valid @RequestParam String email) {
        return ResponseEntity.ok(userService.existsUserByEmail(email));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
        @RequestBody ChangePasswordRequest request,
        @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        
        String userName = principal.getUsername();
        User user = userRepository.findByUserName(userName)
            .orElseThrow(() -> new NoSuchElementException("ユーザーが見つかりません: " + userName));
        String email = user.getEmail();
        userService.changePassword(email, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/display-name")
    public ResponseEntity<String> getDsiplayName(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        String userName = principal.getUsername();
        User currentUser = userRepository.findByUserName(userName)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません"));
        String displayName = currentUser.getUserName();
        return ResponseEntity.ok(displayName);
    }

    @PutMapping("/display-name")
    public ResponseEntity<?> updateDisplayName(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            @RequestBody Map<String, String> request
    ) {
        String userName = principal.getUsername();
        User user = userRepository.findByUserName(userName)
            .orElseThrow(() -> new NoSuchElementException("ユーザーが見つかりません: " + userName));
        String email = user.getEmail();
        String newName = request.get("displayName");
        userService.updateDisplayName(email, newName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/email")
    public ResponseEntity<String> getEmail(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        String userName = principal.getUsername();
        User user = userRepository.findByUserName(userName)
            .orElseThrow(() -> new NoSuchElementException("ユーザーが見つかりません: " + userName));
        String email = user.getEmail();
        return ResponseEntity.ok(email);
    }

    @PutMapping("/email")
    public ResponseEntity<?> updateEmail(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            @RequestBody Map<String, String> request
    ) {
        String userName = principal.getUsername();
        User user = userRepository.findByUserName(userName)
            .orElseThrow(() -> new NoSuchElementException("ユーザーが見つかりません: " + userName));
        String currentEmail = user.getEmail();
        String newEmail = request.get("email");

        if (userService.existsUserByEmail(newEmail)) {
            return ResponseEntity.badRequest().body("このメールアドレスは既に使用されています");
        }

        userService.updateEmail(currentEmail, newEmail);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/viewed")
    public ResponseEntity<List<ViewedMuseumResponse>> getViewedMuseums(
        @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        
        String userName = principal.getUsername();
        User user = userRepository.findByUserName(userName)
            .orElseThrow(() -> new NoSuchElementException("ユーザーが見つかりません: " + userName));
        String email = user.getEmail();
        List<ViewedMuseumResponse> history = userService.getViewedMuseums(email);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/viewed")
    public ResponseEntity<?> saveViewedMuseum(
        @RequestBody ViewedMuseumRequest request,
        @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        
        String userName = principal.getUsername();
        User user = userRepository.findByUserName(userName)
            .orElseThrow(() -> new NoSuchElementException("ユーザーが見つかりません: " + userName));
        String email = user.getEmail();
        userService.saveViewedMuseum(email, request.getMuseumId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/profile-image")
    public ResponseEntity<?> uploadProfileImage(
        @RequestPart("image") MultipartFile image,
        @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {

        String userName = principal.getUsername();
        User user = userRepository.findByUserName(userName)
            .orElseThrow(() -> new NoSuchElementException("ユーザーが見つかりません: " + userName));
        String email = user.getEmail();
        log.info("プロフィール画像アップロードリクエスト: email={}", email); 
        String imageUrl = userService.uploadProfileImage(email, image);
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }

    @GetMapping("/{id}/profile-image")
    public ResponseEntity<?> getProfileImage(@PathVariable Long id) {
        String imageUrl = userService.getProfileImageUrl(id);
        if (imageUrl == null || imageUrl.isEmpty()) {
            return ResponseEntity.ok(Map.of("imageUrl", ""));
        }
        
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }
}