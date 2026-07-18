package com.br.feedbackflow.functions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.br.feedbackflow.config.AWSConfig;
import com.br.feedbackflow.model.Feedback;
import com.br.feedbackflow.service.FeedbackService;
import com.br.feedbackflow.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.sns.SnsClient;

import java.util.HashMap;
import java.util.Map;

public class ReceiveFeedbackHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ReceiveFeedbackHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final FeedbackService feedbackService;
    private final NotificationService notificationService;

    public ReceiveFeedbackHandler(){
        DynamoDbClient dynamoDbClient = AWSConfig.getDynamoDbClient();
        SnsClient snsClient = AWSConfig.getSnsClient();
        SesClient sesClient = AWSConfig.getSesClient();

        this.feedbackService = new FeedbackService(dynamoDbClient);
        this.notificationService = new NotificationService(snsClient, sesClient,
                AWSConfig.getSnsTopicArn(), AWSConfig.getSenderEmail(), AWSConfig.getAdminEmail());
    }
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        logger.info("Received feedback request: {}", input.getBody());

        try{
            Feedback inputFeedback = objectMapper.readValue(input.getBody(), Feedback.class);

            if (inputFeedback.getDescription() == null || inputFeedback.getDescription().trim().isEmpty()){
                return createResponse(400, "{\"message\": \"Description is required\"}");
            }

            if (inputFeedback.getRating() < 0 || inputFeedback.getRating() > 10){
                return createResponse(400, "{\"message\": \"Rating must be between 0 and 10\"}");
            }

            Feedback feedback = new Feedback(inputFeedback.getDescription(), inputFeedback.getRating());

            logger.info("Processing feedback: {}", feedback);

            feedbackService.saveFeedback(feedback);
            logger.info("Feedback saved to DynamoDB successfully");

            if ("CRITICAL".equals(feedback.getUrgency())){
                logger.info("Critical feedback detected - triggering notifications");

                notificationService.publishCriticalFeedback(feedback);
                notificationService.sendUrgencyEmail(feedback);

                logger.info("Critical feedback notifications sent");
            }

            String responseBody = objectMapper.writeValueAsString(feedback);
            return createResponse(201, responseBody);
        } catch (Exception e){
            logger.error("Error processing feedback: {}", e.getMessage(), e);
            return createResponse(500, "{\"message\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    private APIGatewayProxyResponseEvent createResponse(int statusCode, String body) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Methods", "POST, OPTIONS");
        headers.put("Access-Control-Allow-Headers", "Content-Type");

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withBody(body)
                .withHeaders(headers);
    }
}
