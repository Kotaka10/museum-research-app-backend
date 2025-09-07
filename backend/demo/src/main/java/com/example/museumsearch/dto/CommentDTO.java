package com.example.museumsearch.dto;

import lombok.Getter;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Getter
@Builder
public class CommentDTO {
    private Long id;

    @NotNull
    private Long museumId;

    @NotBlank
    @Size(max = 1000)
    private String content;

    private String museumName;
    private String username;
    private String displayName;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
