package com.example.museumsearch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.museumsearch.model.User;
import com.example.museumsearch.model.ViewedMuseum;

public interface ViewedMuseumRepository extends JpaRepository<ViewedMuseum, Long> {
    List<ViewedMuseum> findByUserOrderByViewedAtDesc(User user);
    boolean existsByUserAndMuseumId(User user, Long museumId);
    List<ViewedMuseum> findByUserAndMuseumId(User user, Long museumId);
}
