package com.mikepn.template.v1.repositories;

import com.mikepn.template.v1.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface INotificationRepository extends JpaRepository<Notification , UUID> {
}
