package com.br.feedbackflow.service;

import com.br.feedbackflow.model.Feedback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedbackService {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackService.class);
    private static final String TABLE_NAME = "Feedbacks";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final DynamoDbClient dynamoDbClient;

    public FeedbackService(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public Feedback saveFeedback(Feedback feedback){
        logger.info("Saving feedback to DynamoDB: {}", feedback.getId());

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().s(feedback.getId()).build());
        item.put("description", AttributeValue.builder().s(feedback.getDescription()).build());
        item.put("rating", AttributeValue.builder().n(String.valueOf(feedback.getRating())).build());
        item.put("urgency", AttributeValue.builder().s(feedback.getUrgency()).build());
        item.put("createdAt", AttributeValue.builder().s(feedback.getCreatedAt().format(FORMATTER)).build());
        item.put("updatedAt", AttributeValue.builder().s(feedback.getUpdatedAt().format(FORMATTER)).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();

        dynamoDbClient.putItem(request);
        logger.info("Feedback saved succesfully: {}", feedback.getId());

        return feedback;
    }

    public List<Feedback> getFeedbacksFromLastSevenDays(){
        logger.info("Retrieving feedbacks from the last seven days");

        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build();

        ScanResponse response = dynamoDbClient.scan(scanRequest);
        List<Feedback> feedbacks = new ArrayList<>();

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        for (Map<String, AttributeValue> item : response.items()){
            try {
                Feedback feedback = mapToFeedback(item);
                if (feedback.getCreatedAt() != null && feedback.getCreatedAt().isAfter(sevenDaysAgo)){
                    feedbacks.add(feedback);
                }
            } catch (Exception e){
                logger.warn("Failed to map feedback item: {}", e.getMessage());
            }
        }

        logger.info("Retrieved {} feedbacks from the last seven days", feedbacks.size());
        return feedbacks;
    }

    public List<Feedback> getFeedbackByUrgency(String urgency){
        logger.info("Retrieving feedbacks with urgency:{}", urgency);

        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":urgency", AttributeValue.builder().s(urgency).build());

        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .filterExpression("urgency = :urgency")
                .expressionAttributeValues(expressionValues)
                .build();

        ScanResponse response = dynamoDbClient.scan(scanRequest);
        List<Feedback> feedbacks = new ArrayList<>();

        for (Map<String, AttributeValue> item : response.items()){
            try {
                feedbacks.add(mapToFeedback(item));
            } catch (Exception e){
                logger.warn("Failed to map feedback item: {}", e.getMessage());
            }
        }

        logger.info("Retrieved {} feedbacks with urgency: {}", feedbacks.size(), urgency);
        return  feedbacks;
    }

    private Feedback mapToFeedback(Map<String, AttributeValue> item){
        Feedback feedback = new Feedback();

        if (item.containsKey("id")){
            feedback.setId(item.get("id").s());
        }
        if (item.containsKey("description")) {
            feedback.setDescription(item.get("description").s());
        }
        if (item.containsKey("rating")) {
            feedback.setRating(Integer.parseInt(item.get("rating").n()));
        }
        if (item.containsKey("urgency")) {
            feedback.setUrgency(item.get("urgency").s());
        }
        if (item.containsKey("createdAt")) {
            feedback.setCreatedAt(LocalDateTime.parse(item.get("createdAt").s(), FORMATTER));
        }
        if (item.containsKey("updatedAt")) {
            feedback.setUpdatedAt(LocalDateTime.parse(item.get("updatedAt").s(), FORMATTER));
        }

        return feedback;
    }
}
