package com.mikepn.template.v1.services.impl;

import com.mikepn.template.v1.enums.EMessageStatus;
import com.mikepn.template.v1.enums.IEmailTemplate;
import com.mikepn.template.v1.models.Notification;
import com.mikepn.template.v1.repositories.INotificationRepository;
import com.mikepn.template.v1.standalone.EmailService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationEmailService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEmailService.class);

    private final INotificationRepository notificationRepository;
    private final EmailService emailService;

    @Scheduled(fixedDelay = 5000) // Every 5 seconds after the last execution completes
    @Transactional
    public void sendPendingNotificationEmails() {
        logger.info("Scheduled Task Triggered at {}", LocalDateTime.now());

        List<Notification> pendingNotifications = notificationRepository.findByEmailSentFalse();
        logger.info("Found {} pending notifications", pendingNotifications.size());

        for (Notification notification : pendingNotifications) {
            String to = notification.getEmployee().getProfile().getEmail();
            if (to == null || to.isBlank()) {
                logger.warn("Skipping notification with ID {} due to missing email", notification.getId());
                continue;
            }

            try {
                Map<String, Object> variables = new HashMap<>();
                variables.put("messageContent", notification.getMessageContent());
                variables.put("notificationDate", notification.getCreatedDate());

                // From employee profile
                variables.put("firstName", notification.getEmployee().getProfile().getFirstName());

                // From notification itself
                variables.put("month", notification.getMonth());
                variables.put("year", notification.getYear());

                // Fixed or config string
                variables.put("institution", "Enterprise Resource Planning");

                // Amount: from payslip if available
                if (notification.getPayslip() != null) {
                    variables.put("amount", notification.getPayslip().getNetSalary());
                } else {
                    variables.put("amount", "N/A");
                }

                emailService.sendEmail(
                        to,
                        notification.getEmployee().getProfile().getFullName(),
                        "Salary Payment Notification",
                        IEmailTemplate.SALARY_PAYMENT_NOTIFICATION,
                        variables
                );

                notification.setEmailSent(true);
                notification.setSentAt(LocalDateTime.now());
                notification.setStatus(EMessageStatus.SENT);
                notificationRepository.save(notification);

                logger.info("Email sent and status updated for notification ID {}", notification.getId());

            } catch (MessagingException e) {
                logger.error("Failed to send notification email to {}: {}", to, e.getMessage(), e);
            } catch (Exception ex) {
                logger.error("Unexpected error while sending notification to {}: {}", to, ex.getMessage(), ex);
            }
        }

    }
}
