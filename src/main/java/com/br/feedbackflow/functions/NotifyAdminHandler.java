package com.br.feedbackflow.functions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.br.feedbackflow.config.AWSConfig;
import com.br.feedbackflow.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.sns.SnsClient;

import java.util.List;

public class NotifyAdminHandler implements RequestHandler<SNSEvent, String> {

    private static final Logger logger = LoggerFactory.getLogger(NotifyAdminHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final NotificationService notificationService;

    public NotifyAdminHandler() {
        SnsClient snsClient = AWSConfig.getSnsClient();
        SesClient sesClient = AWSConfig.getSesClient();

        this.notificationService = new NotificationService(
                snsClient,
                sesClient,
                AWSConfig.getSnsTopicArn(),
                AWSConfig.getSenderEmail(),
                AWSConfig.getAdminEmail()
        );
    }

    @Override
    public String handleRequest(SNSEvent input, Context context) {
        LambdaLogger lambdaLogger = context.getLogger();
        lambdaLogger.log("NotifyAdminHandler triggered by SNS event");

        try{
            List<SNSEvent.SNSRecord> records = input.getRecords();

            if (records == null || records.isEmpty()){
                logger.warn("No SNS records found in event");
                return "No records to process";
            }

            for (SNSEvent.SNSRecord record : records){
                String message = record.getSNS().getMessage();
                lambdaLogger.log("Processing SNS message: " + message);

                notificationService.sendUrgencyEmailFromSnsMessage(message);

                logger.info("Admin notification sent for SNS message");
            }

            return "Successfully processed" + records.size() + "notification(s)";

        } catch (Exception e){
            logger.error("Error processing SNS event: {}", e.getMessage(), e);
            lambdaLogger.log("Error: " + e.getMessage());
            return "Error processing notifications: " + e.getMessage();
        }
    }
}
