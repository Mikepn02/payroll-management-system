package com.mikepn.template.v1.config;

import com.mikepn.template.v1.enums.EMessageStatus;
import com.mikepn.template.v1.enums.IEmailTemplate;
import com.mikepn.template.v1.models.Notification;
import com.mikepn.template.v1.repositories.INotificationRepository;
import com.mikepn.template.v1.standalone.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationScheduler {

    private final INotificationRepository notificationRepository;
    private final EmailService emailService;

    @Scheduled(cron = "0 */1 * * * *") // Every minute
    public void runNotificationJob() {
        log.info("🔔 Notification job triggered at {}", LocalDateTime.now());

        List<Notification> pendingNotifications = notificationRepository.findByStatusAndEmailSent(EMessageStatus.PENDING, false);

        if (pendingNotifications.isEmpty()) {
            log.info("No pending notifications at {}", LocalDateTime.now());
            return;
        }

        for (Notification notification : pendingNotifications) {
            try {
                var employee = notification.getEmployee();

                if (employee == null || employee.getProfile() == null) {
                    log.warn("Skipping: Notification ID {} has no employee or profile.", notification.getId());
                    continue;
                }

                Map<String, Object> variables = new HashMap<>();
                variables.put("firstName", employee.getProfile().getFirstName());
                variables.put("month", notification.getMonth());
                variables.put("year", notification.getYear());
                variables.put("institution", "Your Institution Name");
                variables.put("amount", notification.getMessageContent());


                emailService.sendEmail(
                        employee.getProfile().getEmail(),
                        employee.getProfile().getFirstName(),
                        "Salary Payment Notification",
                        IEmailTemplate.SALARY_PAYMENT_NOTIFICATION,
                        variables
                );

                notification.setStatus(EMessageStatus.SENT);
                notification.setEmailSent(true);
                notification.setSentAt(LocalDateTime.now());
                notificationRepository.save(notification);

                log.info("✅ Email sent to employee ID: {}", employee.getId());

            } catch (MessagingException e) {
                log.error("❌ Email failed for notification ID: {}: {}", notification.getId(), e.getMessage());
                notification.setStatus(EMessageStatus.FAILED);
                notificationRepository.save(notification);
            } catch (Exception e) {
                log.error("❌ Unexpected error for notification ID: {}: {}", notification.getId(), e.getMessage());
            }
        }
    }
}
