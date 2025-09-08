package com.example.museumsearch.controller;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.museumsearch.dto.CommentDTO;
import com.example.museumsearch.mapper.CommentMapper;
import com.example.museumsearch.model.Comment;
import com.example.museumsearch.service.CommentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/comments")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCommentController {
    private final CommentService commentService;
    private final CommentMapper commentMapper;

    @GetMapping
    public ResponseEntity<Page<CommentDTO>> getAllComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Comment> commentPage = commentService.findAllComments(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        Page<CommentDTO> dtoPage = commentPage.map(commentMapper::toDTO);
        return ResponseEntity.ok(dtoPage);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentDTO> updateComment(@PathVariable Long id, @RequestBody CommentDTO dto) {
        Comment comment = commentService.findById(id);
        comment.updateContent(dto.getContent());
        comment.updateUpdatedAt(LocalDateTime.now());
        Comment updated = commentService.saveComment(comment);
        return ResponseEntity.ok(commentMapper.toDTO(updated));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CommentDTO>> searchComments(
        @RequestParam String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

        Page<Comment> comments = commentService.searchComments(keyword, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        Page<CommentDTO> dtoPage = comments.map(commentMapper::toDTO);
        return ResponseEntity.ok(dtoPage);
    }
}
