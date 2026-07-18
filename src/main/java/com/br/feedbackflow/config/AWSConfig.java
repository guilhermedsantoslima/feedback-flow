package com.br.feedbackflow.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.sns.SnsClient;

public class AWSConfig {

    private static final Logger logger = LoggerFactory.getLogger(AWSConfig.class);
    private static final String REGION = System.getenv("AWS_REGION") != null
            ? System.getenv("AWS_REGION"): "us-east-1";

    private static DynamoDbClient dynamoDbClient;
    private static SnsClient snsClient;
    private static SesClient sesClient;

    public static DynamoDbClient getDynamoDbClient(){
        if (dynamoDbClient == null){
            logger.info("Initializing DynamoDB client in region: {}", REGION);
            dynamoDbClient = DynamoDbClient.builder()
                    .region(Region.of(REGION))
                    .build();
        }
        return dynamoDbClient;
    }

    public static SnsClient getSnsClient(){
        if (snsClient == null){
            logger.info("Initializing SNS client in region: {}", REGION);
            snsClient = SnsClient.builder()
                    .region(Region.of(REGION))
                    .build();
        }
        return snsClient;
    }

    public static SesClient getSesClient() {
        if (sesClient == null) {
            logger.info("Initializing SES client in region: {}", REGION);
            sesClient = SesClient.builder()
                    .region(Region.of(REGION))
                    .build();
        }
        return sesClient;
    }

    public static String getSnsTopicArn(){
        return  System.getenv("SNS_TOPIC_ARN") != null
                ? System.getenv("SNS_TOPIC_ARN") :"arn:aws:sns:us-east-1:000000000000:FeedbacksCriticos";
    }

    public static String getSenderEmail() {
        return System.getenv("SENDER_EMAIL") != null
                ? System.getenv("SENDER_EMAIL")
                : "noreply@feedbackflow.com";
    }

    public static String getAdminEmail() {
        return System.getenv("ADMIN_EMAIL") != null
                ? System.getenv("ADMIN_EMAIL")
                : "admin@feedbackflow.com";
    }
}
