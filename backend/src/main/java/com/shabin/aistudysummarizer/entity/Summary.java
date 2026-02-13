package com.shabin.aistudysummarizer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "summaries", indexes = {
        @Index(name = "idx_summaries_user_id", columnList = "user_id"),
        @Index(name = "idx_summaries_document_id", columnList = "document_id"),
        @Index(name = "idx_summaries_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Summary {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(columnDefinition = "jsonb", nullable = false)
    private String summaryJson;

    @Column(nullable = false)
    private String modelUsed;

    private Integer tokensUsed;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
