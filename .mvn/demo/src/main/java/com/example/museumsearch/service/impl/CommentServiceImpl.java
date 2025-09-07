package com.example.museumsearch.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.museumsearch.model.Comment;
import com.example.museumsearch.repository.CommentRepository;
import com.example.museumsearch.service.CommentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    @Override
    public Comment saveComment(Comment comment) {
        log.info("コメントを保存します: {}", comment);
        return commentRepository.save(comment);
    }

    @Override
    public Comment updateComment(Long id, Comment comment) {
        Comment existing = findById(id);    
        existing.updateContent(comment.getContent());
        existing.updateUpdatedAt(LocalDateTime.now());
        log.info("コメントを更新します: id={}, 新しい内容={}", id, comment.getContent());
        return commentRepository.save(existing);
    }

    @Override
    public void deleteComment(Long id) {
        if (!commentRepository.existsById(id)) {
            log.warn("削除対象のコメントが存在しません: id={}", id);
            throw new NoSuchElementException("指定されたIDのコメントが見つかりません" + id);
        }
        log.info("コメントを削除します: id={}", id);
        commentRepository.deleteById(id);
    }

    @Override
    public Comment findById(Long id) {
        return commentRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("指定されたIDのコメントが見つかりません" + id));
    }

    @Override
    public List<Comment> getCommentsByMuseumId(Long museumId) {
        return commentRepository.findByMuseumId(museumId);
    }

    @Override
    public List<Comment> getCommentsByUsername(String username) {
        return commentRepository.findByUsernameOrderByCreatedAtDesc(username);
    }

    @Override
    public Page<Comment> findAllComments(Pageable pageable) {
        return commentRepository.findAll(pageable);
    }

    @Override
    public List<Comment> searchByKeyword(String keyword) {
        return commentRepository.searchByUsernameOrContent(keyword);
    }

    @Override
    public Page<Comment> searchComments(String keyword, Pageable pageable) {
        return commentRepository.searchByKeyword(keyword, pageable);
    }
}
