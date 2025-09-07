package com.example.museumsearch.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.example.museumsearch.model.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByMuseumId(Long museumId);
    List<Comment> findByUsernameOrderByCreatedAtDesc(String username);

    @NonNull
    Page<Comment> findAll(@NonNull Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.username LIKE %:keyword% OR c.content LIKE %:keyword%")
    List<Comment> searchByUsernameOrContent(@Param("keyword") String keyword);

    @Query("SELECT c FROM Comment c WHERE c.username LIKE %:keyword% OR c.content LIKE %:keyword% OR c.museum.name LIKE %:keyword%")
    Page<Comment> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
