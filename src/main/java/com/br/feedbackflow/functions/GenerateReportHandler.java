package com.br.feedbackflow.functions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.br.feedbackflow.config.AWSConfig;
import com.br.feedbackflow.model.Feedback;
import com.br.feedbackflow.model.WeeklyReport;
import com.br.feedbackflow.service.FeedbackService;
import com.br.feedbackflow.service.NotificationService;
import com.br.feedbackflow.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.sns.SnsClient;

import java.util.List;

public class GenerateReportHandler implements RequestHandler<ScheduledEvent, String> {

    private static final Logger logger = LoggerFactory.getLogger(GenerateReportHandler.class);
    private final FeedbackService feedbackService;
    private final ReportService reportService;
    private final NotificationService notificationService;

    public GenerateReportHandler() {
        DynamoDbClient dynamoDbClient = AWSConfig.getDynamoDbClient();
        SnsClient snsClient = AWSConfig.getSnsClient();
        SesClient sesClient = AWSConfig.getSesClient();

        this.feedbackService = new FeedbackService(dynamoDbClient);
        this.reportService = new ReportService();
        this.notificationService = new NotificationService(
                snsClient,
                sesClient,
                AWSConfig.getSnsTopicArn(),
                AWSConfig.getSenderEmail(),
                AWSConfig.getAdminEmail()
        );
    }

    @Override
    public String handleRequest(ScheduledEvent input, Context context) {
        LambdaLogger lambdaLogger = context.getLogger();
        lambdaLogger.log("GenerateReportHandler triggered by EventBridge schedule");


        try {
            logger.info("Starting weekly report generation");

            List<Feedback> feedbacks = feedbackService.getFeedbacksFromLastSevenDays();
            logger.info("Retrieved {} feedbacks from the last seven days", feedbacks.size());

            WeeklyReport report = reportService.generateWeeklyReport(feedbacks);
            logger.info("Weekly report generated: {}", report);

            String htmlReport = reportService.generateHtmlReport(report);
            logger.info("HTML report generated successfully");

            notificationService.sendWeeklyReportEmail(htmlReport);
            logger.info("Weekly report email sent to admin");

            lambdaLogger.log("Weekly report generated and sent successfully");
            return "Weekly report generated and sent successfully. Total feedbacks: " + report.getTotalFeedbacks();
        } catch (Exception e){
            logger.error("Error generating weekly report: {}", e.getMessage(), e);
            lambdaLogger.log("Error: " + e.getMessage());
            return "Error generating weekly report:" + e.getMessage();
        }
    }
}
