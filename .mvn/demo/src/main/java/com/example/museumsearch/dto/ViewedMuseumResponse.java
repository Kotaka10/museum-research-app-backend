package com.example.museumsearch.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ViewedMuseumResponse {
    private MuseumDTO museum;
    private LocalDateTime viewedAt;
}
