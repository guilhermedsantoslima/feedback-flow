package com.br.feedbackflow.service;

import com.br.feedbackflow.model.Feedback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final SnsClient snsClient;
    private final SesClient sesClient;
    private final String snsTopicArn;
    private final String senderEmail;
    private final String adminEmail;

    public NotificationService(SnsClient snsClient, SesClient sesClient, String snsTopicArn, String senderEmail, String adminEmail) {
        this.snsClient = snsClient;
        this.sesClient = sesClient;
        this.snsTopicArn = snsTopicArn;
        this.senderEmail = senderEmail;
        this.adminEmail = adminEmail;
    }

    public void publishCriticalFeedback(Feedback feedback){
        logger.info("Publishing critical feedback to SNS: {}", feedback.getId());

        String message = String.format(
                "CRITICAL FEEDBACK ALERT\n" +
                        "========================\n" +
                        "Feedback ID : %s\n" +
                        "Description : %s\n" +
                        "Rating      : %d/10\n" +
                        "Urgency     : %s\n" +
                        "Created At  : %s\n",
                feedback.getId(),
                feedback.getDescription(),
                feedback.getRating(),
                feedback.getUrgency(),
                feedback.getCreatedAt()
        );

        String subject = String.format("CRITICAL Feedback Alert - Rating %d/10", feedback.getRating());

        PublishRequest request = PublishRequest.builder()
                .topicArn(snsTopicArn)
                .subject(subject)
                .message(message)
                .build();

        PublishResponse response = snsClient.publish(request);
        logger.info("Critical feedback published to SNS. Message ID: {}", response.messageId());
    }

    public void sendUrgencyEmail(Feedback feedback){
        logger.info("Sending urgency email to admin: {}", adminEmail);

        String htmlBody = String.format(
                "<html><body style='font-family: Arial, sans-serif;'>" +
                        "<div style='max-width: 600px; margin: auto; border: 1px solid #ddd; border-radius: 8px;'>" +
                        "<div style='background-color: #d32f2f; color: white; padding: 20px; border-radius: 8px 8px 0 0;'>" +
                        "<h1 style='margin: 0;'>⚠️ CRITICAL FEEDBACK ALERT</h1></div>" +
                        "<div style='padding: 20px;'>" +
                        "<table style='width: 100%%; border-collapse: collapse;'>" +
                        "<tr><td style='padding: 8px; font-weight: bold; border-bottom: 1px solid #eee;'>Feedback ID</td>" +
                        "<td style='padding: 8px; border-bottom: 1px solid #eee;'>%s</td></tr>" +
                        "<tr><td style='padding: 8px; font-weight: bold; border-bottom: 1px solid #eee;'>Description</td>" +
                        "<td style='padding: 8px; border-bottom: 1px solid #eee;'>%s</td></tr>" +
                        "<tr><td style='padding: 8px; font-weight: bold; border-bottom: 1px solid #eee;'>Rating</td>" +
                        "<td style='padding: 8px; border-bottom: 1px solid #eee; color: #d32f2f; font-weight: bold;'>%d/10</td></tr>" +
                        "<tr><td style='padding: 8px; font-weight: bold; border-bottom: 1px solid #eee;'>Urgency</td>" +
                        "<td style='padding: 8px; border-bottom: 1px solid #eee; color: #d32f2f; font-weight: bold;'>%s</td></tr>" +
                        "<tr><td style='padding: 8px; font-weight: bold; border-bottom: 1px solid #eee;'>Created At</td>" +
                        "<td style='padding: 8px; border-bottom: 1px solid #eee;'>%s</td></tr>" +
                        "</table></div></div></body></html>",
                feedback.getId(),
                feedback.getDescription(),
                feedback.getRating(),
                feedback.getUrgency(),
                feedback.getCreatedAt()
        );

        String subject = String.format("[URGENT] Critical Feedback - Rating %d/10", feedback.getRating());

        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .source(senderEmail)
                .destination(Destination.builder().toAddresses(adminEmail).build())
                .message(Message.builder()
                        .subject(Content.builder().data(subject).build())
                        .body(Body.builder()
                                .html(Content.builder().data(htmlBody).build())
                                .build())
                        .build())
                .build();

        sesClient.sendEmail(emailRequest);
        logger.info("Urgency email sent succesfully to: {}", adminEmail);
    }

    public void sendUrgencyEmailFromSnsMessage(String snsMessage) {
        logger.info("Sending urgency email from SNS message to admin: {}", adminEmail);

        String htmlBody = String.format(
                "<html><body style='font-family: Arial, sans-serif;'>" +
                        "<div style='max-width: 600px; margin: auto; border: 1px solid #ddd; border-radius: 8px;'>" +
                        "<div style='background-color: #d32f2f; color: white; padding: 20px; border-radius: 8px 8px 0 0;'>" +
                        "<h1 style='margin: 0;'>&#9888;&#65039; CRITICAL FEEDBACK ALERT</h1></div>" +
                        "<div style='padding: 20px;'>" +
                        "<pre style='white-space: pre-wrap; font-family: Arial, sans-serif; font-size: 14px;'>%s</pre>" +
                        "</div></div></body></html>",
                snsMessage
        );

        String subject = "[URGENT] Critical Feedback Alert - FeedbackFlow";

        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .source(senderEmail)
                .destination(Destination.builder().toAddresses(adminEmail).build())
                .message(Message.builder()
                        .subject(Content.builder().data(subject).build())
                        .body(Body.builder()
                                .html(Content.builder().data(htmlBody).build())
                                .build())
                        .build())
                .build();

        sesClient.sendEmail(emailRequest);
        logger.info("Urgency email sent successfully from SNS message to: {}", adminEmail);
    }

    public void sendWeeklyReportEmail(String htmlReport){
        logger.info("Sending weekly report email to admin: {}", adminEmail);

        String subject = "Weekly Feedback Report - FeedbackFlow";

        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .source(senderEmail)
                .destination(Destination.builder().toAddresses(adminEmail).build())
                .message(Message.builder()
                        .subject(Content.builder().data(htmlReport).build())
                        .body(Body.builder()
                                .html(Content.builder().data(htmlReport).build())
                                .build())
                        .build())
                .build();

        sesClient.sendEmail(emailRequest);
        logger.info("Weekly report email sent succesfully to: {}", adminEmail);
    }
}
