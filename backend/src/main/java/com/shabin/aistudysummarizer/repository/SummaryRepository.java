package com.shabin.aistudysummarizer.repository;

import com.shabin.aistudysummarizer.entity.Summary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SummaryRepository extends JpaRepository<Summary, UUID> {
    List<Summary> findByUserId(UUID userId);
}
