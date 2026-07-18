package com.br.feedbackflow.service;

import com.br.feedbackflow.model.Feedback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class NotificationServiceTest {

    private SnsClient snsClient;
    private SesClient sesClient;
    private NotificationService notificationService;

    private static final String SNS_TOPIC_ARN = "arn:aws:sns:us-east-1:123456789012:FeedbacksCriticos";
    private static final String SENDER_EMAIL = "noreply@feedbackflow.com";
    private static final String ADMIN_EMAIL = "admin@feedbackflow.com";

    @BeforeEach
    void setUp() {
        snsClient = mock(SnsClient.class);
        sesClient = mock(SesClient.class);
        notificationService = new NotificationService(
                snsClient, sesClient, SNS_TOPIC_ARN, SENDER_EMAIL, ADMIN_EMAIL
        );
    }

    @Test
    @DisplayName("publishCriticalFeedback should publish to correct SNS topic")
    void publishCriticalFeedbackShouldPublishToCorrectTopic() {
        Feedback feedback = new Feedback("Critical issue", 1);

        when(snsClient.publish(any(PublishRequest.class)))
                .thenReturn(PublishResponse.builder().messageId("msg-123").build());

        notificationService.publishCriticalFeedback(feedback);

        verify(snsClient, times(1)).publish(any(PublishRequest.class));

        ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
        verify(snsClient).publish(captor.capture());

        PublishRequest captured = captor.getValue();
        assertEquals(SNS_TOPIC_ARN, captured.topicArn());
        assertTrue(captured.message().contains("Critical issue"));
        assertTrue(captured.message().contains("1/10"));
        assertTrue(captured.message().contains("CRITICAL"));
        assertTrue(captured.subject().contains("CRITICAL"));
    }

    @Test
    @DisplayName("sendUrgencyEmail should send email to admin via SES")
    void sendUrgencyEmailShouldSendEmailToAdmin() {
        Feedback feedback = new Feedback("Urgent feedback", 2);

        when(sesClient.sendEmail(any(SendEmailRequest.class)))
                .thenReturn(SendEmailResponse.builder().messageId("email-123").build());

        notificationService.sendUrgencyEmail(feedback);

        verify(sesClient, times(1)).sendEmail(any(SendEmailRequest.class));

        ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesClient).sendEmail(captor.capture());

        SendEmailRequest captured = captor.getValue();
        assertEquals(SENDER_EMAIL, captured.source());
        assertTrue(captured.destination().toAddresses().contains(ADMIN_EMAIL));
        assertTrue(captured.message().subject().data().contains("URGENT"));
        assertTrue(captured.message().body().html().data().contains("Urgent feedback"));
        assertTrue(captured.message().body().html().data().contains("CRITICAL"));
    }

    @Test
    @DisplayName("sendWeeklyReportEmail should send HTML report to admin")
    void sendWeeklyReportEmailShouldSendHtmlReport() {
        String htmlReport = "<html><body><h1>Weekly Report</h1><p>Total: 10</p></body></html>";

        when(sesClient.sendEmail(any(SendEmailRequest.class)))
                .thenReturn(SendEmailResponse.builder().messageId("email-456").build());

        notificationService.sendWeeklyReportEmail(htmlReport);

        verify(sesClient, times(1)).sendEmail(any(SendEmailRequest.class));

        ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesClient).sendEmail(captor.capture());

        SendEmailRequest captured = captor.getValue();
        assertEquals(SENDER_EMAIL, captured.source());
        assertTrue(captured.destination().toAddresses().contains(ADMIN_EMAIL));
        assertTrue(captured.message().subject().data().contains("Weekly"));
        assertTrue(captured.message().body().html().data().contains("Weekly Report"));
    }

    @Test
    @DisplayName("sendUrgencyEmailFromSnsMessage should format SNS message into HTML email")
    void sendUrgencyEmailFromSnsMessageShouldFormatSnsMessage() {
        String snsMessage = "CRITICAL FEEDBACK ALERT\nFeedback ID: uuid-123\nRating: 1/10";

        when(sesClient.sendEmail(any(SendEmailRequest.class)))
                .thenReturn(SendEmailResponse.builder().messageId("email-789").build());

        notificationService.sendUrgencyEmailFromSnsMessage(snsMessage);

        verify(sesClient, times(1)).sendEmail(any(SendEmailRequest.class));

        ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesClient).sendEmail(captor.capture());

        SendEmailRequest captured = captor.getValue();
        assertTrue(captured.message().body().html().data().contains("uuid-123"));
        assertTrue(captured.message().body().html().data().contains("1/10"));
    }
}
