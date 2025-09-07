package com.example.museumsearch.service.impl;

import com.example.museumsearch.dto.MuseumDTO;
import com.example.museumsearch.model.Museum;
import com.example.museumsearch.model.MuseumStatus;
import com.example.museumsearch.model.Role;
import com.example.museumsearch.model.User;
import com.example.museumsearch.repository.MuseumRepository;
import com.example.museumsearch.service.MuseumService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MuseumServiceImpl implements MuseumService {

    private final MuseumRepository museumRepository;

    @Override
    public List<Museum> findAllMuseums() {
        return museumRepository.findAll();
    }

    @Override
    public Museum findMuseumById(Long id) {
        return museumRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "指定されたIDの美術館が見つかりません: " + id));
    }

    @Override
    public Museum createMuseum(Museum museum) {
        log.info("美術館を保存します: {}", museum);
        return museumRepository.save(museum);
    }

    @Override
    public Museum updateMuseum(Long id, Museum updatedMuseum, User user) {
        Museum existingMuseum = museumRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "美術館が見つかりません: " + id));

        boolean isAdmin = user.getRoles() == Role.ADMIN;
        boolean isOwner = existingMuseum.getCreatedBy().getId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "編集権限がありません");
        }

        existingMuseum.updateName(updatedMuseum.getName());
        existingMuseum.updateAddress(updatedMuseum.getAddress());
        existingMuseum.updatePrefecture(updatedMuseum.getPrefecture());
        existingMuseum.updatePhoneNumber(updatedMuseum.getPhoneNumber());
        existingMuseum.updateExhibition(updatedMuseum.getExhibition());
        existingMuseum.updateExhibitionImage(updatedMuseum.getExhibitionImage());
        existingMuseum.updateMuseumUrl(updatedMuseum.getMuseumUrl());
        existingMuseum.updateExhibitionUrl(updatedMuseum.getExhibitionUrl());
        existingMuseum.updateStartDate(updatedMuseum.getStartDate());
        existingMuseum.updateEndDate(updatedMuseum.getEndDate());
        existingMuseum.updateDescription(updatedMuseum.getDescription());
        existingMuseum.updateOpeningHours(updatedMuseum.getOpeningHours());
        existingMuseum.updateClosingDays(updatedMuseum.getClosingDays());
        existingMuseum.updateAdmissionFee(updatedMuseum.getAdmissionFee());
        existingMuseum.updateAccess(updatedMuseum.getAccess());
        existingMuseum.updateLatitude(updatedMuseum.getLatitude());
        existingMuseum.updateLongitude(updatedMuseum.getLongitude());
        existingMuseum.updateCategory(updatedMuseum.getCategory());
        if (isOwner) existingMuseum.updateStatus(MuseumStatus.PENDING);

        return museumRepository.save(existingMuseum);
    }

    @Override
    public List<Museum> getApprovedMuseums() {
        return museumRepository.findByStatus(MuseumStatus.APPROVED);
    }

    @Override
    public List<Museum> getPendingMuseums() {
        return museumRepository.findByStatus(MuseumStatus.PENDING);
    }

    @Override
    public Museum approveMuseum(Long id) {
        Museum museum = museumRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "指定されたIDの美術館が見つかりません: " + id));
        museum.updateStatus(MuseumStatus.APPROVED);
        return museumRepository.save(museum);
    }

    @Override
    public Museum rejectMuseum(Long id) {
        Museum museum = museumRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "指定されたIDの美術館が見つかりません: " + id));
        museum.updateStatus(MuseumStatus.REJECTED);
        return museumRepository.save(museum);
    }

    @Override
    public void deleteMuseum(Long id) {
        if (!museumRepository.existsById(id)) {
            log.warn("削除対象の美術館が見つかりません: id={}", id);
            throw new NoSuchElementException("指定されたIDの美術館が見つかりません: " + id);
        }
        log.info("美術館を削除します: id={}", id);
        museumRepository.deleteById(id);
    }

    @Override
    public Page<Museum> searchMuseums(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) return Page.empty(pageable);

        return museumRepository
            .findByNameContainingIgnoreCaseOrExhibitionContainingIgnoreCaseOrAddressContainingIgnoreCase(keyword, keyword, keyword, pageable);
    }

    @Override
    public List<MuseumDTO> getNearbyMuseums(double lat, double lon) {
        List<Museum> museums = museumRepository.findAll();

        return museums.stream()
            .map(museum -> {
                double distance = calculateDistance(lat, lon, museum.getLatitude(), museum.getLongitude());
                return MuseumDTO.builder()
                    .id(museum.getId())
                    .name(museum.getName())
                    .address(museum.getAddress())
                    .prefecture(museum.getPrefecture())
                    .phoneNumber(museum.getPhoneNumber())
                    .exhibition(museum.getExhibition())
                    .museumUrl(museum.getMuseumUrl())
                    .exhibitionUrl(museum.getExhibitionUrl())
                    .startDate(museum.getStartDate())
                    .endDate(museum.getEndDate())
                    .description(museum.getDescription())
                    .openingHours(museum.getOpeningHours())
                    .closingDays(museum.getClosingDays())
                    .admissionFee(museum.getAdmissionFee())
                    .access(museum.getAccess())
                    .latitude(museum.getLatitude())
                    .longitude(museum.getLongitude())
                    .category(museum.getCategory())
                    .distance(distance)
                    .build();
            })
            .sorted(Comparator.comparingDouble(
                m -> calculateDistance(lat, lon, m.getLatitude(), m.getLongitude())
            ))
            .collect(Collectors.toList());
    }

    private double calculateDistance(double lat, double lon, double museumLatitude, double museumLongitude) {
        double earthRadiusKm = 6371;
        double deltaLatitudeRad = Math.toRadians(museumLatitude - lat);
        double deltaLongitudeRad =  Math.toRadians(museumLongitude - lon);

        double haversineFormulaValue = Math.sin(deltaLatitudeRad / 2) * Math.sin(deltaLatitudeRad / 2) +
                                       Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(museumLatitude)) * 
                                       Math.sin(deltaLongitudeRad) * Math.sin(deltaLongitudeRad / 2);

        double  centralAngleRad = 2 * Math.atan2(Math.sqrt(haversineFormulaValue), Math.sqrt(1 - haversineFormulaValue));
        return earthRadiusKm * centralAngleRad;
    }
} 