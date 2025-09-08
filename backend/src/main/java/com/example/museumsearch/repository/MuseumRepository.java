package com.example.museumsearch.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.museumsearch.model.Museum;
import com.example.museumsearch.model.MuseumStatus;
import com.example.museumsearch.model.User;

@Repository
public interface MuseumRepository extends JpaRepository<Museum, Long>{
    List<Museum> findByStatus(MuseumStatus status);
    List<Museum> findByCreatedByIsNotNullAndCreatedBy(User user);
    boolean existsByName(String name);
    boolean existsByNameAndExhibition(String name, String exhibition);
    Page<Museum> findByCategoryContaining(String name, Pageable pageable);
    Page<Museum> findByPrefectureEqualsIgnoreCase(String address, Pageable pageable);
    Page<Museum> findByNameContainingIgnoreCaseOrExhibitionContainingIgnoreCaseOrAddressContainingIgnoreCase(
        String name, String exihibition, String address, Pageable pageable
    );
}
