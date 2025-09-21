package com.example.museumsearch.service.impl;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.museumsearch.dto.ViewedMuseumResponse;
import com.example.museumsearch.mapper.MuseumMapper;
import com.example.museumsearch.model.Museum;
import com.example.museumsearch.model.Role;
import com.example.museumsearch.model.User;
import com.example.museumsearch.model.ViewedMuseum;
import com.example.museumsearch.repository.MuseumRepository;
import com.example.museumsearch.repository.UserRepository;
import com.example.museumsearch.repository.ViewedMuseumRepository;
import com.example.museumsearch.security.JwtProvider;
import com.example.museumsearch.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final PasswordEncoder passwordEncoder;
    private final MuseumMapper museumMapper;
    private final MuseumRepository museumRepository;
    private final UserRepository userRepository;
    private final ViewedMuseumRepository viewedMuseumRepository;
    private final JwtProvider jwtProvider;

    @Override
    public void registerUser(String email, String password, String displayName) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("既に登録されているメールアドレスです: " + email);
        }
        User user = new User();
        user.updateEmail(email);
        user.updatePassword(passwordEncoder.encode(password));
        user.updateDisplayName(displayName);
        user.updateRoles(Role.USER);
        log.info("ユーザー登録: {}" + user.getEmail());
        userRepository.save(user);
    }

    @Override
    public User editUser(Long id, User updateUser) {
        User existingUser = findUserById(id);

        if (updateUser.getUserName() != null) {
            existingUser.updateDisplayName(updateUser.getUserName());
        }
        log.info("ユーザー情報を更新します: {}", id);
        return userRepository.save(existingUser);
    }

    @Override
    public void updateDisplayName(String email, String newDisplayName) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません"));
        user.updateDisplayName(newDisplayName);
        userRepository.save(user);
    }

    @Override
    public void updateEmail(String currentEmail, String newEmail) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません"));
        user.updateEmail(newEmail);
        userRepository.save(user);
    }

    @Override
    public void deleteUserById(Long id) {
        if (!userRepository.existsById(id)) {
            log.warn("削除対象のユーザーが存在しません: id={}", id);
            throw new NoSuchElementException("指定されたIDのユーザーが見つかりません" + id);
        }
        log.info("ユーザーを削除します: id={}", id);
        userRepository.deleteById(id);
    }

    @Override
    public void deleteUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません"));
        log.info("ユーザーを削除します: email={}", email);
        userRepository.delete(user);
    }

    @Override
    public void deleteUserByUserName(String userName) {
        User user = userRepository.findByUserName(userName)
            .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません"));
        log.info("ユーザーを削除します: userName={}", userName);
        userRepository.delete(user);
    }

    @Override
    public User findUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("指定されたIDのユーザーが見つかりません" + id));
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new NoSuchElementException("ユーザーが見つかりません: " + email));
    }

    @Override
    public User findUserByUserName(String userName) {
        return userRepository.findByUserName(userName)
            .orElseThrow(() -> new NoSuchElementException("ユーザーが見つかりません: " + userName));
    }

    @Override
    public boolean existsUserByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public String login(String userNameOrEmail, String rawPassword) {
        User user = userRepository.findByEmail(userNameOrEmail)
            .or(() -> userRepository.findByUserName(userNameOrEmail))
            .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("パスワードが違います");
        }
        return jwtProvider.generateToken(user.getUserName(), List.of("ROLE_" + user.getRoles()));
    }

    @Override
    public User getCurrentUser(String token) {
        String email = jwtProvider.getEmailFromToken(token);
        return userRepository.findByEmail(email).orElseThrow();
    }

    @Override
    public void changePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + email));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("現在のパスワードが一致しません");
        }
        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("新しいパスワードは6文字以上で入力してください");
        }
        log.info("パスワードを変更します: email={}", email);
        user.updatePassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public List<ViewedMuseumResponse> getViewedMuseums(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + email));
        
        List<ViewedMuseum> histories = viewedMuseumRepository.findByUserOrderByViewedAtDesc(user);
        log.info("閲覧履歴を取得します: email={}, 履歴数={}", email, histories.size());

        Map<Long, ViewedMuseum> uniqueMap = new LinkedHashMap<>();
        for (ViewedMuseum history : histories) {
            Long museumId = history.getMuseum().getId();
            if (!uniqueMap.containsKey(museumId)) {
                uniqueMap.put(museumId, history);
            }
        }

        List<ViewedMuseumResponse> uniqueList = uniqueMap.values().stream()
            .map(history -> new ViewedMuseumResponse(museumMapper.toDTO(history.getMuseum()), history.getViewedAt()))
            .limit(10)
            .toList();
        
        return uniqueList;
    }

    @Override
    public void saveViewedMuseum(String email, Long museumId) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + email));
        
        Museum museum = museumRepository.findById(museumId)
            .orElseThrow(() -> new NoSuchElementException("指定されたIDの美術館が見つかりません: " + museumId));

        ViewedMuseum history = new ViewedMuseum(user, museum, LocalDateTime.now());
        
        log.info("閲覧した美術館を保存します: email={}, museumId={}", email, museumId);
        viewedMuseumRepository.save(history);
    }

    @Override
    public String uploadProfileImage(String email, MultipartFile image) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + email));

        String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
        Path imagePath = Path.of("uploads/profile-images", fileName);

        try {
            Files.createDirectories(imagePath.getParent());
            Files.write(imagePath, image.getBytes());
        } catch (IOException e) {
            log.error("画像のアップロードに失敗しました: {}", e.getMessage());
            throw new RuntimeException("画像のアップロードに失敗しました");
        }

        String imageUrl = "/uploads/profile-images/" + fileName;
        user.updateProfileImageUrl(imageUrl);
        userRepository.save(user);

        return imageUrl;
    }

    @Override
    public String getProfileImageUrl(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません"));

        return user.getProfileImageUrl();
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> searchUsers(String keyword) {
        return userRepository.findAll().stream()
            .filter(user -> user.getEmail().contains(keyword) || 
                            (user.getUserName() != null && user.getUserName().contains(keyword)))
            .toList();
    }

    @Override
    public Page<User> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        return userRepository.searchByKeyword(keyword, pageable);
    }
}
