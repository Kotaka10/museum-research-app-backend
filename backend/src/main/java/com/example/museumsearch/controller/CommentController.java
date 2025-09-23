package com.example.museumsearch.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.museumsearch.dto.CommentDTO;
import com.example.museumsearch.mapper.CommentMapper;
import com.example.museumsearch.model.Comment;
import com.example.museumsearch.service.CommentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final CommentMapper commentMapper;
    
    @PostMapping
    public ResponseEntity<CommentDTO> createComment(
        @Valid @RequestBody CommentDTO dto,
        @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
            
        log.info("=== POST /api/comments に到達しました ===");
        String username = principal.getUsername();
        log.info("認証済みユーザーがコメントします: {}", username);

        dto = CommentDTO.builder()
            .museumId(dto.getMuseumId())
            .content(dto.getContent())
            .username(username)
            .build();

        Comment comment = commentMapper.toEntity(dto);
        Comment saved = commentService.saveComment(comment);
        return ResponseEntity.ok(commentMapper.toDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentDTO> updateComment(
        @PathVariable Long id,
        @RequestBody CommentDTO dto,
        @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) {

        Comment comment = commentService.findById(id);

        if (!comment.getUsername().equals(user.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        comment.updateContent(dto.getContent());
        comment.updateUpdatedAt(LocalDateTime.now());
        Comment updated = commentService.saveComment(comment);
        log.info("コメントを更新します: id={}, 新しい内容={}", id, comment.getContent());
        return ResponseEntity.ok(commentMapper.toDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(
        @PathVariable Long id,
        @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) {
        Comment comment = commentService.findById(id);

        if (!comment.getUsername().equals(user.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        log.info("コメントを削除します: id={}", id);
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/museum/{museumId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByMuseumId(@PathVariable Long museumId) {
        List<Comment> comments = commentService.getCommentsByMuseumId(museumId);

        List<CommentDTO> dtoList = comments.stream()
            .map(commentMapper::toDTO)
            .toList();
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/user")
    public ResponseEntity<List<CommentDTO>> getCommentsByUser(@AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        String email = user.getUsername();
        log.info("ユーザー {} のコメントを取得します", email);

        List<Comment> comments = commentService.getCommentsByUsername(email);
        List<CommentDTO> dtoList = comments.stream()
            .map(commentMapper::toDTO)
            .toList();
        return ResponseEntity.ok(dtoList);
    }
}
