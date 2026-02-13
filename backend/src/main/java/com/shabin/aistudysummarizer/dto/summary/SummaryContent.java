package com.shabin.aistudysummarizer.dto.summary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SummaryContent {
    private String executiveSummary;
    private List<String> sectionSummary;
    private List<KeyTerm> keyTerms;
    private List<Mcq> mcqs;
    private List<Flashcard> flashcards;
    private List<String> examInsights;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeyTerm {
        private String term;
        private String definition;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Mcq {
        private String question;
        private List<String> options;
        private String answer;
        private String explanation;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Flashcard {
        private String front;
        private String back;
    }
}
