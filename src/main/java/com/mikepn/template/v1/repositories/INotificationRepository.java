package com.mikepn.template.v1.repositories;

import com.mikepn.template.v1.enums.EMessageStatus;
import com.mikepn.template.v1.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface INotificationRepository extends JpaRepository<Notification , UUID> {

    List<Notification> findByStatusAndEmailSent(EMessageStatus status, Boolean emailSent);
    List<Notification> findByEmailSentFalse();
}
