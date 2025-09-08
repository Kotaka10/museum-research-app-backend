package com.example.museumsearch.controller;

import com.example.museumsearch.dto.MuseumDTO;
import com.example.museumsearch.mapper.MuseumMapper;
import com.example.museumsearch.model.Museum;
import com.example.museumsearch.model.MuseumStatus;
import com.example.museumsearch.model.User;
import com.example.museumsearch.repository.MuseumRepository;
import com.example.museumsearch.repository.UserRepository;
import com.example.museumsearch.service.MuseumService;
import com.example.museumsearch.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/museums")
@RequiredArgsConstructor
public class MuseumController {
    
    private final MuseumRepository museumRepository;
    private final UserRepository userRepository;
    private final MuseumService museumService;
    private final MuseumMapper museumMapper;
    private final UserService userService;

    @GetMapping("/all")
    public ResponseEntity<List<MuseumDTO>> getAllMuseums() {
        List<Museum> museums = museumService.findAllMuseums();
        return ResponseEntity.ok(museums.stream().map(museumMapper::toDTO).toList());
    }

    @GetMapping("/results")
    public ResponseEntity<Page<MuseumDTO>> searchMuseums(
        @RequestParam(required = false) String keyword,
        @PageableDefault(sort = "startDate", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<Museum> results = museumService.searchMuseums(keyword, pageable);
        Page<MuseumDTO> dtoPage = results.map(museumMapper::toDTO);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/prefecture/{prefecture}")
    public ResponseEntity<Page<MuseumDTO>> filterMuseumByPrefecture(
        @PathVariable String prefecture,
        @PageableDefault(sort = "startDate", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<Museum> foundMuseums = museumRepository.findByPrefectureEqualsIgnoreCase(prefecture, pageable);

        Page<MuseumDTO> filterd = foundMuseums.map(museumMapper::toDTO);
        return ResponseEntity.ok(filterd);
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<MuseumDTO>> getNearbyMuseum(
        @RequestParam(required = false) double lat, @RequestParam(required = false) double lon
    ) {
        return ResponseEntity.ok(museumService.getNearbyMuseums(lat, lon));
    }

    @GetMapping("/gardens/{category}")
    public ResponseEntity<Page<MuseumDTO>> getGardens(
        @PathVariable String category,
        @PageableDefault(sort = "startDate", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<Museum> foundMuseums = museumRepository.findByCategoryContaining(category, pageable);
        Page<MuseumDTO> dtoMuseums = foundMuseums.map(museumMapper::toDTO);
        return ResponseEntity.ok(dtoMuseums);
    }

    @GetMapping("/photos/{category}")
    public ResponseEntity<Page<MuseumDTO>> getPhotos(
        @PathVariable String category,
        @PageableDefault(sort = "startDate", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<Museum> foundMuseums = museumRepository.findByCategoryContaining(category, pageable);
        Page<MuseumDTO> dtoMuseums = foundMuseums.map(museumMapper::toDTO);
        return ResponseEntity.ok(dtoMuseums);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<MuseumDTO> createMuseum(
        @RequestBody MuseumDTO dto,
        @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) {
        String email = user.getUsername();
        User managedUser = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません"));

        Museum museum = museumMapper.toEntity(dto);
        museum.updateCreatedBy(managedUser);
        museum.updateStatus(MuseumStatus.PENDING);

        log.info("美術館を保存します: {}", museum);
        Museum saved = museumRepository.save(museum);
        return ResponseEntity.ok(museumMapper.toDTO(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MuseumDTO> getMuseumById(@PathVariable Long id) {
         Museum foundMuseum = museumService.findMuseumById(id);
         return ResponseEntity.ok(museumMapper.toDTO(foundMuseum));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Museum> updateMuseum(
        @PathVariable Long id,
        @RequestBody Museum updatedMuseum,
        @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal
    ) {
        String email = principal.getUsername();
        User user = userService.findUserByEmail(email);
        Museum updated = museumService.updateMuseum(id, updatedMuseum, user);
        return ResponseEntity.ok(updated);
}

    @GetMapping("/mypage")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<MuseumDTO>> getMyMuseums(
         @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) {
        String email = user.getUsername();
        User managedUser = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません"));

        List<Museum> museums = museumRepository.findByCreatedByIsNotNullAndCreatedBy(managedUser);
        List<MuseumDTO> dtos = museums.stream()
            .map(museumMapper::toDTO)
            .toList();
        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMuseum(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("Authorities: {}", auth.getAuthorities());
        log.info("美術館を削除します: id={}", id);
        museumService.deleteMuseum(id);
        return ResponseEntity.noContent().build();
    }
}
