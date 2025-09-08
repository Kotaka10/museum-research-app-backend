package com.example.museumsearch.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;

@Entity
@Getter
public class ViewedMuseum {
    
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Museum museum;

    private LocalDateTime viewedAt;

    public ViewedMuseum(User user, Museum museum, LocalDateTime viewedAt) {
        this.user = user;
        this.museum = museum;
        this.viewedAt = viewedAt;
    }

    protected ViewedMuseum() {}
}
