package com.br.feedbackflow.service;

import com.br.feedbackflow.model.Feedback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class FeedbackServiceTest {
    private DynamoDbClient dynamoDbClient;
    private FeedbackService feedbackService;

    @BeforeEach
    void setUp() {
        dynamoDbClient = mock(DynamoDbClient.class);
        feedbackService = new FeedbackService(dynamoDbClient);
    }

    @Test
    @DisplayName("saveFeedback should call putItem with correct attributes")
    void saveFeedbackShouldCallPutItemWithCorrectAttributes() {
        Feedback feedback = new Feedback("Test feedback", 3);

        when(dynamoDbClient.putItem(any(PutItemRequest.class)))
                .thenReturn(PutItemResponse.builder().build());

        Feedback result = feedbackService.saveFeedback(feedback);

        assertNotNull(result);
        verify(dynamoDbClient, times(1)).putItem(any(PutItemRequest.class));

        ArgumentCaptor<PutItemRequest> captor = ArgumentCaptor.forClass(PutItemRequest.class);
        verify(dynamoDbClient).putItem(captor.capture());

        PutItemRequest captured = captor.getValue();
        assertEquals("Feedbacks", captured.tableName());

        Map<String, AttributeValue> item = captured.item();
        assertEquals(feedback.getId(), item.get("id").s());
        assertEquals("Test feedback", item.get("description").s());
        assertEquals("3", item.get("rating").n());
        assertEquals("CRITICAL", item.get("urgency").s());
    }

    @Test
    @DisplayName("getFeedbacksByUrgency should filter by urgency level")
    void getFeedbacksByUrgencyShouldFilterByUrgency() {
        Map<String, AttributeValue> item1 = new HashMap<>();
        item1.put("id", AttributeValue.builder().s("uuid-1").build());
        item1.put("description", AttributeValue.builder().s("Bad feedback").build());
        item1.put("rating", AttributeValue.builder().n("2").build());
        item1.put("urgency", AttributeValue.builder().s("CRITICAL").build());
        item1.put("createdAt", AttributeValue.builder().s("2026-07-15T10:00:00").build());
        item1.put("updatedAt", AttributeValue.builder().s("2026-07-15T10:00:00").build());

        ScanResponse scanResponse = ScanResponse.builder()
                .items(item1)
                .build();

        when(dynamoDbClient.scan(any(ScanRequest.class)))
                .thenReturn(scanResponse);

        List<Feedback> result = feedbackService.getFeedbackByUrgency("CRITICAL");

        assertEquals(1, result.size());
        assertEquals("uuid-1", result.get(0).getId());
        assertEquals("Bad feedback", result.get(0).getDescription());
        assertEquals(2, result.get(0).getRating());
        assertEquals("CRITICAL", result.get(0).getUrgency());

        verify(dynamoDbClient, times(1)).scan(any(ScanRequest.class));
    }

    @Test
    @DisplayName("getFeedbacksByUrgency should return empty list when no results")
    void getFeedbacksByUrgencyShouldReturnEmptyList() {
        ScanResponse scanResponse = ScanResponse.builder()
                .items(Collections.emptyList())
                .build();

        when(dynamoDbClient.scan(any(ScanRequest.class)))
                .thenReturn(scanResponse);

        List<Feedback> result = feedbackService.getFeedbackByUrgency("POSITIVE");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getFeedbacksFromLast7Days should filter by date")
    void getFeedbacksFromLast7DaysShouldFilterByDate() {
        // Recent feedback (within 7 days)
        Map<String, AttributeValue> recentItem = new HashMap<>();
        recentItem.put("id", AttributeValue.builder().s("uuid-recent").build());
        recentItem.put("description", AttributeValue.builder().s("Recent feedback").build());
        recentItem.put("rating", AttributeValue.builder().n("5").build());
        recentItem.put("urgency", AttributeValue.builder().s("NORMAL").build());
        recentItem.put("createdAt", AttributeValue.builder().s(LocalDateTime.now().minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).build());
        recentItem.put("updatedAt", AttributeValue.builder().s(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).build());

        // Old feedback (more than 7 days ago)
        Map<String, AttributeValue> oldItem = new HashMap<>();
        oldItem.put("id", AttributeValue.builder().s("uuid-old").build());
        oldItem.put("description", AttributeValue.builder().s("Old feedback").build());
        oldItem.put("rating", AttributeValue.builder().n("8").build());
        oldItem.put("urgency", AttributeValue.builder().s("POSITIVE").build());
        oldItem.put("createdAt", AttributeValue.builder().s(LocalDateTime.now().minusDays(20).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).build());
        oldItem.put("updatedAt", AttributeValue.builder().s(LocalDateTime.now().minusDays(20).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).build());

        ScanResponse scanResponse = ScanResponse.builder()
                .items(recentItem, oldItem)
                .build();

        when(dynamoDbClient.scan(any(ScanRequest.class)))
                .thenReturn(scanResponse);

        List<Feedback> result = feedbackService.getFeedbacksFromLastSevenDays();

        assertEquals(1, result.size());
        assertEquals("uuid-recent", result.get(0).getId());
        assertEquals("Recent feedback", result.get(0).getDescription());
    }
}
