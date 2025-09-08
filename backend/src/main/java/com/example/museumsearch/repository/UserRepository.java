package com.example.museumsearch.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.museumsearch.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByDisplayName(String displayName);

    @Query("SELECT u FROM User u WHERE u.email LIKE %:keyword% OR u.displayName LIKE %:keyword%")
    Page<User> searchByKeyword(String keyword, Pageable pageable);
}
