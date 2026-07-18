package com.br.feedbackflow.service;

import com.br.feedbackflow.model.Feedback;
import com.br.feedbackflow.model.WeeklyReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReportServiceTest {

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService();
    }

    @Test
    @DisplayName("generateWeeklyReport should calculate total feedbacks correctly")
    void generateWeeklyReportShouldCalculateTotal() {
        List<Feedback> feedbacks = Arrays.asList(
                new Feedback("Feedback 1", 3),
                new Feedback("Feedback 2", 7),
                new Feedback("Feedback 3", 5)
        );

        WeeklyReport report = reportService.generateWeeklyReport(feedbacks);

        assertEquals(3, report.getTotalFeedbacks());
    }

    @Test
    @DisplayName("generateWeeklyReport should calculate average rating correctly")
    void generateWeeklyReportShouldCalculateAverage() {
        List<Feedback> feedbacks = Arrays.asList(
                new Feedback("Feedback 1", 3),
                new Feedback("Feedback 2", 7),
                new Feedback("Feedback 3", 5)
        );

        WeeklyReport report = reportService.generateWeeklyReport(feedbacks);

        assertEquals(5.0, report.getAverageRating(), 0.01);
    }

    @Test
    @DisplayName("generateWeeklyReport should distribute urgency correctly")
    void generateWeeklyReportShouldDistributeUrgency() {
        List<Feedback> feedbacks = Arrays.asList(
                new Feedback("Critical 1", 1),
                new Feedback("Critical 2", 2),
                new Feedback("Normal 1", 5),
                new Feedback("Positive 1", 8),
                new Feedback("Positive 2", 9),
                new Feedback("Positive 3", 10)
        );

        WeeklyReport report = reportService.generateWeeklyReport(feedbacks);

        assertEquals(2, report.getUrgencyDistribution().get("CRITICAL"));
        assertEquals(1, report.getUrgencyDistribution().get("NORMAL"));
        assertEquals(3, report.getUrgencyDistribution().get("POSITIVE"));
    }

    @Test
    @DisplayName("generateWeeklyReport with empty list should return zero stats")
    void generateWeeklyReportWithEmptyListShouldReturnZeros() {
        WeeklyReport report = reportService.generateWeeklyReport(Collections.emptyList());

        assertEquals(0, report.getTotalFeedbacks());
        assertEquals(0.0, report.getAverageRating(), 0.01);
        assertEquals(0, report.getUrgencyDistribution().get("CRITICAL"));
        assertEquals(0, report.getUrgencyDistribution().get("NORMAL"));
        assertEquals(0, report.getUrgencyDistribution().get("POSITIVE"));
    }

    @Test
    @DisplayName("generateWeeklyReport should count feedbacks per day correctly")
    void generateWeeklyReportShouldCountFeedbacksPerDay() {
        Feedback f1 = new Feedback("Feedback 1", 5);
        f1.setCreatedAt(LocalDateTime.of(2026, 7, 15, 10, 0));

        Feedback f2 = new Feedback("Feedback 2", 7);
        f2.setCreatedAt(LocalDateTime.of(2026, 7, 15, 14, 0));

        Feedback f3 = new Feedback("Feedback 3", 8);
        f3.setCreatedAt(LocalDateTime.of(2026, 7, 16, 9, 0));

        List<Feedback> feedbacks = Arrays.asList(f1, f2, f3);

        WeeklyReport report = reportService.generateWeeklyReport(feedbacks);

        assertEquals(2, report.getFeedbacksPerDay().get("2026-07-15"));
        assertEquals(1, report.getFeedbacksPerDay().get("2026-07-16"));
        assertEquals(2, report.getFeedbacksPerDay().size());
    }

    @Test
    @DisplayName("generateHtmlReport should contain key HTML elements")
    void generateHtmlReportShouldContainKeyElements() {
        List<Feedback> feedbacks = Arrays.asList(
                new Feedback("Test feedback", 3),
                new Feedback("Good feedback", 9)
        );

        WeeklyReport report = reportService.generateWeeklyReport(feedbacks);
        String html = reportService.generateHtmlReport(report);

        assertTrue(html.contains("<html>"));
        assertTrue(html.contains("Weekly Feedback Report"));
        assertTrue(html.contains("Total Feedbacks"));
        assertTrue(html.contains("2"));
        assertTrue(html.contains("Critical"));
        assertTrue(html.contains("Positive"));
        assertTrue(html.contains("</html>"));
    }

    @Test
    @DisplayName("generateHtmlReport should include feedbacks per day table")
    void generateHtmlReportShouldIncludeFeedbacksPerDayTable() {
        Feedback f1 = new Feedback("Feedback 1", 5);
        f1.setCreatedAt(LocalDateTime.of(2026, 7, 17, 10, 0));

        List<Feedback> feedbacks = Collections.singletonList(f1);

        WeeklyReport report = reportService.generateWeeklyReport(feedbacks);
        String html = reportService.generateHtmlReport(report);

        assertTrue(html.contains("2026-07-17"));
        assertTrue(html.contains("Feedbacks Per Day"));
    }
}
