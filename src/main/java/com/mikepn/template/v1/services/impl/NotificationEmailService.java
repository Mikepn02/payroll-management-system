package com.mikepn.template.v1.services.impl;

import com.mikepn.template.v1.enums.EMessageStatus;
import com.mikepn.template.v1.enums.IEmailTemplate;
import com.mikepn.template.v1.models.Notification;
import com.mikepn.template.v1.repositories.INotificationRepository;
import com.mikepn.template.v1.standalone.EmailService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationEmailService {

    private final INotificationRepository notificationRepository;
    private final EmailService emailService;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void sendPendingNotificationEmails() {
        List<Notification> pendingNotifications = notificationRepository.findByEmailSentFalse();

        for (Notification notification : pendingNotifications) {
            String to = notification.getEmployee().getProfile().getEmail();
            if (to == null || to.isBlank()) {
                continue;
            }

            try {
                Map<String, Object> variables = new HashMap<>();
                variables.put("messageContent", notification.getMessageContent());
                variables.put("notificationDate", notification.getCreatedDate());

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

            } catch (MessagingException e) {
                System.err.println("Failed to send notification email to " + to + ": " + e.getMessage());
                // optionally log with Logger
            }
        }
    }
}
