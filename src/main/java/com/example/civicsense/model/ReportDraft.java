package com.example.civicsense.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class ReportDraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phone;

    private String state; 

    private String issueType;

    private String location;

    @Column(length = 2000)
    private String description;

    private String contact;

    @Column(length = 2000)
    private String mediaUrls;

    private LocalDateTime updatedAt;
}

