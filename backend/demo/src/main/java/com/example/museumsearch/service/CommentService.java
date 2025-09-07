package com.example.museumsearch.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.museumsearch.model.Comment;

public interface CommentService {
    Comment saveComment(Comment comment);
    Comment updateComment(Long id, Comment comment);
    void deleteComment(Long id);
    List<Comment> getCommentsByMuseumId(Long museumId);
    Comment findById(Long id);
    List<Comment> getCommentsByUsername(String username);
    Page<Comment> findAllComments(Pageable pageable);
    List<Comment> searchByKeyword(String keyword);
    Page<Comment> searchComments(String keyword, Pageable pageable);
}