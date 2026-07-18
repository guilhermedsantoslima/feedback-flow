package com.br.feedbackflow.service;

import com.br.feedbackflow.model.Feedback;
import com.br.feedbackflow.model.WeeklyReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public WeeklyReport generateWeeklyReport(List<Feedback> feedbacks){
        logger.info("Generating weekly report from {} feedbacks", feedbacks.size());

        WeeklyReport report = new WeeklyReport("Last seven days");
        report.setTotalFeedbacks(feedbacks.size());

        Integer totalRating = 0;
        Map<String, Integer> feedbacksPerDay = new TreeMap<>();

        for (Feedback feedback : feedbacks){
            totalRating += feedback.getRating();

            report.incrementUrgency(feedback.getUrgency());

            if (feedback.getCreatedAt() != null){
                String day = feedback.getCreatedAt().format(DATE_FORMATTER);
                feedbacksPerDay.put(day, feedbacksPerDay.getOrDefault(day, 0)+ 1);
            }
        }

        Double averageRating = feedbacks.isEmpty() ? 0.0: (double) totalRating / feedbacks.size();
        report.setAverageRating(averageRating);
        report.setFeedbacksPerDay(feedbacksPerDay);

        logger.info("Weekly report generated: {}", report);
        return report;
    }

    public String generateHtmlReport(WeeklyReport report){
        logger.info("Generating HTML report for email");

        StringBuilder feedbacksPerDayRows = new StringBuilder();

        for (Map.Entry<String, Integer> entry: report.getFeedbacksPerDay().entrySet()){
            feedbacksPerDayRows.append(String.format(
                    "<tr><td style='padding: 8px; border-bottom: 1px solid #eee;'>%s</td>" +
                            "<td style='padding: 8px; border-bottom: 1px solid #eee; text-align: center;'>%d</td></tr>",
                    entry.getKey(), entry.getValue()
            ));
        }

        return String.format(
                "<html><body style='font-family: Arial, sans-serif;'>" +
                        "<div style='max-width: 700px; margin: auto; border: 1px solid #ddd; border-radius: 8px;'>" +
                        "<div style='background-color: #1976d2; color: white; padding: 20px; border-radius: 8px 8px 0 0;'>" +
                        "<h1 style='margin: 0;'>📊 Weekly Feedback Report</h1>" +
                        "<p style='margin: 5px 0 0 0; opacity: 0.9;'>Period: %s</p></div>" +
                        "<div style='padding: 20px;'>" +
                        "<h2 style='color: #333;'>Summary</h2>" +
                        "<table style='width: 100%%; border-collapse: collapse;'>" +
                        "<tr><td style='padding: 8px; font-weight: bold; border-bottom: 1px solid #eee;'>Total Feedbacks</td>" +
                        "<td style='padding: 8px; border-bottom: 1px solid #eee;'>%d</td></tr>" +
                        "<tr><td style='padding: 8px; font-weight: bold; border-bottom: 1px solid #eee;'>Average Rating</td>" +
                        "<td style='padding: 8px; border-bottom: 1px solid #eee;'>%.2f/10</td></tr>" +
                        "<tr><td style='padding: 8px; font-weight: bold; border-bottom: 1px solid #eee; color: #d32f2f;'>Critical</td>" +
                        "<td style='padding: 8px; border-bottom: 1px solid #eee; color: #d32f2f;'>%d</td></tr>" +
                        "<tr><td style='padding: 8px; font-weight: bold; border-bottom: 1px solid #eee; color: #f57c00;'>Normal</td>" +
                        "<td style='padding: 8px; border-bottom: 1px solid #eee; color: #f57c00;'>%d</td></tr>" +
                        "<tr><td style='padding: 8px; font-weight: bold; border-bottom: 1px solid #eee; color: #388e3c;'>Positive</td>" +
                        "<td style='padding: 8px; border-bottom: 1px solid #eee; color: #388e3c;'>%d</td></tr>" +
                        "</table>" +
                        "<h2 style='color: #333; margin-top: 30px;'>Feedbacks Per Day</h2>" +
                        "<table style='width: 100%%; border-collapse: collapse;'>" +
                        "<tr style='background-color: #f5f5f5;'>" +
                        "<td style='padding: 8px; font-weight: bold; border-bottom: 2px solid #ddd;'>Date</td>" +
                        "<td style='padding: 8px; font-weight: bold; border-bottom: 2px solid #ddd; text-align: center;'>Count</td></tr>" +
                        "%s" +
                        "</table></div></div></body></html>",
                report.getWeek(),
                report.getTotalFeedbacks(),
                report.getAverageRating(),
                report.getUrgencyDistribution().getOrDefault("CRITICAL", 0),
                report.getUrgencyDistribution().getOrDefault("NORMAL", 0),
                report.getUrgencyDistribution().getOrDefault("POSITIVE", 0),
                feedbacksPerDayRows.toString()
        );
    }
}
