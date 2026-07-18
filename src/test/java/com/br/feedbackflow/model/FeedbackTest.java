package com.br.feedbackflow.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FeedbackTest {
    @Nested
    @DisplayName("calculateUrgency tests")
    class CalculateUrgencyTests {

        @Test
        @DisplayName("Should return CRITICAL when rating is below 4")
        void shouldReturnCriticalWhenRatingBelow4() {
            assertEquals("CRITICAL", Feedback.calculateUrgency(0));
            assertEquals("CRITICAL", Feedback.calculateUrgency(1));
            assertEquals("CRITICAL", Feedback.calculateUrgency(3));
        }

        @Test
        @DisplayName("Should return NORMAL when rating is between 4 and 6")
        void shouldReturnNormalWhenRatingBetween4And6() {
            assertEquals("NORMAL", Feedback.calculateUrgency(4));
            assertEquals("NORMAL", Feedback.calculateUrgency(5));
            assertEquals("NORMAL", Feedback.calculateUrgency(6));
        }

        @Test
        @DisplayName("Should return POSITIVE when rating is 7 or above")
        void shouldReturnPositiveWhenRating7OrAbove() {
            assertEquals("POSITIVE", Feedback.calculateUrgency(7));
            assertEquals("POSITIVE", Feedback.calculateUrgency(8));
            assertEquals("POSITIVE", Feedback.calculateUrgency(10));
        }
    }

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor should initialize id and timestamps")
        void defaultConstructorShouldInitializeFields() {
            Feedback feedback = new Feedback();

            assertNotNull(feedback.getId());
            assertNotNull(feedback.getCreatedAt());
            assertNotNull(feedback.getUpdatedAt());
        }

        @Test
        @DisplayName("Parameterized constructor should set description, rating and urgency")
        void parameterizedConstructorShouldSetFields() {
            Feedback feedback = new Feedback("Great service", 9);

            assertEquals("Great service", feedback.getDescription());
            assertEquals(9, feedback.getRating());
            assertEquals("POSITIVE", feedback.getUrgency());
            assertNotNull(feedback.getId());
            assertNotNull(feedback.getCreatedAt());
        }

        @Test
        @DisplayName("Parameterized constructor with low rating should set CRITICAL urgency")
        void parameterizedConstructorWithLowRatingShouldSetCritical() {
            Feedback feedback = new Feedback("Terrible experience", 2);

            assertEquals(2, feedback.getRating());
            assertEquals("CRITICAL", feedback.getUrgency());
        }
    }

    @Nested
    @DisplayName("Setter tests")
    class SetterTests {

        @Test
        @DisplayName("setRating should recalculate urgency automatically")
        void setRatingShouldRecalculateUrgency() {
            Feedback feedback = new Feedback("Test", 8);

            assertEquals("POSITIVE", feedback.getUrgency());

            feedback.setRating(2);
            assertEquals("CRITICAL", feedback.getUrgency());

            feedback.setRating(5);
            assertEquals("NORMAL", feedback.getUrgency());
        }
    }

    @Test
    @DisplayName("toString should contain all fields")
    void toStringShouldContainAllFields() {
        Feedback feedback = new Feedback("Test feedback", 7);

        String result = feedback.toString();

        assertTrue(result.contains("FEEDBACK DETAILS"));
        assertTrue(result.contains("Test feedback"));
        assertTrue(result.contains("7/10"));
        assertTrue(result.contains("POSITIVE"));
    }
}
