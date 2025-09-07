package com.example.museumsearch.dto;

import java.time.LocalDate;

import com.example.museumsearch.model.MuseumStatus;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MuseumDTO {

    private Long id;
    private String name;
    private String address;
    private String prefecture;
    private String exhibition;
    private String imageProvider;
    private String exhibitionImage;
    private String museumUrl;
    private String exhibitionUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private String phoneNumber;
    private String openingHours;
    private String closingDays;
    private String admissionFee;
    private String access;
    private Double latitude;
    private Double longitude;
    private String category;
    private Double distance;
    private MuseumStatus status;
    private String createdByUsername;
}
