package com.example.museumsearch.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.museumsearch.dto.MuseumDTO;
import com.example.museumsearch.model.Museum;
import com.example.museumsearch.model.User;

public interface MuseumService {
    List<Museum> findAllMuseums();
    Museum findMuseumById(Long id);
    Museum createMuseum(Museum museum);
    Museum updateMuseum(Long id, Museum museum, User user);
    List<Museum> getApprovedMuseums();
    List<Museum> getPendingMuseums();
    Museum approveMuseum(Long id);
    Museum rejectMuseum(Long id);
    void deleteMuseum(Long id);
    Page<Museum> searchMuseums(String keyword, Pageable pageable);
    List<MuseumDTO> getNearbyMuseums(double lat, double lon);
}
