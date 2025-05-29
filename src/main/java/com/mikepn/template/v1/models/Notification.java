package com.mikepn.template.v1.models;

import com.mikepn.template.v1.common.AbstractEntity;
import com.mikepn.template.v1.enums.EMessageStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // Optional: Link to Payslip if needed
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payslip_id")
    private Payslip payslip;

    @Column(name = "message_content", length = 1000)
    private String messageContent;

    private Integer month;

    private Integer year;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Enumerated(EnumType.STRING)
    private EMessageStatus status = EMessageStatus.PENDING;

    @Column(name = "email_sent")
    private Boolean emailSent = false;
}
