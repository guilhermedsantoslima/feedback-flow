package com.br.feedbackflow.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

public class Feedback {

    @JsonProperty("id")
    private String id;
    @JsonProperty("description")
    private String description;
    @JsonProperty("rating")
    private int rating;
    @JsonProperty("urgency")
    private String urgency;
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    public Feedback() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Feedback(String description, int rating) {
        this();
        this.description = description;
        this.rating = rating;
        this.urgency = calculateUrgency(rating);
    }

    public static String calculateUrgency(int rating) {
        if (rating < 4) {
            return "CRITICAL";
        } else if (rating < 7) {
            return "NORMAL";
        } else {
            return "POSITIVE";
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
        this.urgency = calculateUrgency(rating);
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return String.format(
                "\n╔══════════════════════════════════════════╗\n" +
                        "║           FEEDBACK DETAILS               ║\n" +
                        "╠══════════════════════════════════════════╣\n" +
                        "║ ID          : %s\n" +
                        "║ Description : %s\n" +
                        "║ Rating      : %d/10\n" +
                        "║ Urgency     : %s\n" +
                        "║ Created At  : %s\n" +
                        "║ Updated At  : %s\n" +
                        "╚══════════════════════════════════════════╝",
                id, description, rating, urgency, createdAt, updatedAt
        );
    }
}
