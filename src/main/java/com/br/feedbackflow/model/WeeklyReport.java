package com.br.feedbackflow.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WeeklyReport {
    @JsonProperty("id")
    private String id;
    @JsonProperty("week")
    private String week;
    @JsonProperty("totalFeedbacks")
    private Integer totalFeedbacks;
    @JsonProperty("averageRating")
    private Double averageRating;
    @JsonProperty("urgencyDistribution")
    private Map<String,Integer> urgencyDistribution;
    @JsonProperty("feedbacksPerDay")
    private Map<String,Integer> feedbacksPerDay;
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    public WeeklyReport(){
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.urgencyDistribution = new HashMap<>();
        this.feedbacksPerDay = new HashMap<>();

        this.urgencyDistribution.put("CRITICAL", 0);
        this.urgencyDistribution.put("NORMAL", 0);
        this.urgencyDistribution.put("POSITIVE", 0);
    }

    public WeeklyReport(String week){
        this();
        this.week = week;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public Integer getTotalFeedbacks() {
        return totalFeedbacks;
    }

    public void setTotalFeedbacks(Integer totalFeedbacks) {
        this.totalFeedbacks = totalFeedbacks;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Map<String, Integer> getUrgencyDistribution() {
        return urgencyDistribution;
    }

    public void setUrgencyDistribution(Map<String, Integer> urgencyDistribution) {
        this.urgencyDistribution = urgencyDistribution;
    }

    public Map<String, Integer> getFeedbacksPerDay() {
        return feedbacksPerDay;
    }

    public void setFeedbacksPerDay(Map<String, Integer> feedbacksPerDay) {
        this.feedbacksPerDay = feedbacksPerDay;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void incrementUrgency(String urgency) {
        if (urgencyDistribution.containsKey(urgency)) {
            urgencyDistribution.put(urgency, urgencyDistribution.get(urgency) + 1);
        }
    }

    public void incrementFeedbacksPerDay(String day) {
        feedbacksPerDay.put(day, feedbacksPerDay.getOrDefault(day, 0) + 1);
    }

    @Override
    public String toString() {
        return String.format(java.util.Locale.US,
                "\n╔══════════════════════════════════════════╗\n" +
                        "║          WEEKLY REPORT SUMMARY           ║\n" +
                        "╠══════════════════════════════════════════╣\n" +
                        "║ ID              : %s\n" +
                        "║ Week            : %s\n" +
                        "║ Total Feedbacks : %d\n" +
                        "║ Average Rating  : %.2f/10\n" +
                        "║ Critical        : %d\n" +
                        "║ Normal          : %d\n" +
                        "║ Positive        : %d\n" +
                        "║ Created At      : %s\n" +
                        "╚══════════════════════════════════════════╝",
                id, week, totalFeedbacks, averageRating,
                urgencyDistribution.getOrDefault("CRITICAL", 0),
                urgencyDistribution.getOrDefault("NORMAL", 0),
                urgencyDistribution.getOrDefault("POSITIVE", 0),
                createdAt
        );
    }
}
