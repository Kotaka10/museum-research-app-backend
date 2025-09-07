package com.example.museumsearch.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "museums")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Museum {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "名称は必須です")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "住所は必須です")
    @Size(max = 200)
    private String address;

    @Size(max = 10)
    private String prefecture;

    @Size(max = 100)
    private String exhibition;

    @Size(max = 100)
    @Column(name = "image_provider")
    private String imageProvider;

    @Size(max = 100)
    @Column(name = "exhibition_image")
    private String exhibitionImage;

    @Size(max = 100)
    @Column(name = "museum_url")
    private String museumUrl;

    @Size(max = 230)
    @Column(name ="exhibition_url")
    private String exhibitionUrl;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Size(max = 1500)
    private String description;

    @Size(max = 15)
    @Column(name ="phone_number")
    private String phoneNumber;

    @Size(max = 500)
    @Column(name ="opening_hours")
    private String openingHours;

    @Size(max = 500)
    @Column(name ="closing_days")
    private String closingDays;

    @Size(max = 1000)
    @Column(name ="adminssion_fee")
    private String admissionFee;

    @Size(max = 500)
    private String access;

    private Double latitude;
    private Double longitude;

    @Size(max = 100)
    private String category;

    private Double distance;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MuseumStatus status = MuseumStatus.APPROVED;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    public void updateName(String name) {
        this.name = name;
    }

    public void updateAddress(String address) {
        this.address = address;
    }

    public void updatePrefecture(String prefecture) {
        this.prefecture = prefecture;
    }
    
    public void updateExhibition(String exhibition) {
        this.exhibition = exhibition;
    }

    public void updateImageProvider(String imageProvider) {
        this.imageProvider = imageProvider;
    }

    public void updateExhibitionImage(String exhibitionImage) {
        this.exhibitionImage = exhibitionImage;
    }

    public void updateMuseumUrl(String museumUrl) {
        this.museumUrl = museumUrl;
    }

    public void updateExhibitionUrl(String exhibitionUrl) {
        this.exhibitionUrl = exhibitionUrl;
    }

    public void updateStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void updateEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updatePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void updateOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public void updateClosingDays(String closingDays) {
        this.closingDays = closingDays;
    }

    public void updateAdmissionFee(String admissionFee) {
        this.admissionFee = admissionFee;
    }

    public void updateAccess(String access) {
        this.access = access;
    }

    public void updateLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void updateLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void updateCategory(String category) {
        this.category = category;
    }

    public void updateStatus(MuseumStatus status) {
        this.status = status;
    }

    public void updateCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
}
