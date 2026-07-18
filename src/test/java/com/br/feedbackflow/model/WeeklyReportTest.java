package com.br.feedbackflow.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WeeklyReportTest {

    private WeeklyReport report;

    @BeforeEach
    void setUp() {
        report = new WeeklyReport("Test Week");
    }

    @Test
    @DisplayName("Constructor should initialize urgency distribution with zeros")
    void constructorShouldInitializeUrgencyDistribution() {
        assertEquals(0, report.getUrgencyDistribution().get("CRITICAL"));
        assertEquals(0, report.getUrgencyDistribution().get("NORMAL"));
        assertEquals(0, report.getUrgencyDistribution().get("POSITIVE"));
    }

    @Test
    @DisplayName("incrementUrgency should increment correct category")
    void incrementUrgencyShouldIncrementCorrectCategory() {
        report.incrementUrgency("CRITICAL");
        report.incrementUrgency("CRITICAL");
        report.incrementUrgency("NORMAL");
        report.incrementUrgency("POSITIVE");
        report.incrementUrgency("POSITIVE");
        report.incrementUrgency("POSITIVE");

        assertEquals(2, report.getUrgencyDistribution().get("CRITICAL"));
        assertEquals(1, report.getUrgencyDistribution().get("NORMAL"));
        assertEquals(3, report.getUrgencyDistribution().get("POSITIVE"));
    }

    @Test
    @DisplayName("incrementUrgency should ignore unknown urgency types")
    void incrementUrgencyShouldIgnoreUnknownTypes() {
        report.incrementUrgency("UNKNOWN");

        assertEquals(0, report.getUrgencyDistribution().get("CRITICAL"));
        assertEquals(0, report.getUrgencyDistribution().get("NORMAL"));
        assertEquals(0, report.getUrgencyDistribution().get("POSITIVE"));
    }

    @Test
    @DisplayName("incrementFeedbacksPerDay should count correctly per day")
    void incrementFeedbacksPerDayShouldCountCorrectly() {
        report.incrementFeedbacksPerDay("2026-07-15");
        report.incrementFeedbacksPerDay("2026-07-15");
        report.incrementFeedbacksPerDay("2026-07-16");
        report.incrementFeedbacksPerDay("2026-07-17");

        assertEquals(2, report.getFeedbacksPerDay().get("2026-07-15"));
        assertEquals(1, report.getFeedbacksPerDay().get("2026-07-16"));
        assertEquals(1, report.getFeedbacksPerDay().get("2026-07-17"));
        assertEquals(3, report.getFeedbacksPerDay().size());
    }

    @Test
    @DisplayName("toString should contain report summary")
    void toStringShouldContainReportSummary() {
        report.setTotalFeedbacks(10);
        report.setAverageRating(7.5);
        report.incrementUrgency("CRITICAL");
        report.incrementUrgency("POSITIVE");

        String result = report.toString();

        assertTrue(result.contains("WEEKLY REPORT SUMMARY"));
        assertTrue(result.contains("10"));
        assertTrue(result.contains("7.50"));
        assertTrue(result.contains("Critical"));
    }
}
