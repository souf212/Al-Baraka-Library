package com.baraka.library.entity;
import jakarta.persistence.*;
@Entity @Table(name="reminder_logs")
public class ReminderLog {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="loan_id") Loan loan;
 public String reminderType;
 public java.time.LocalDateTime preparedAt;
 public java.time.LocalDateTime whatsappOpenedAt;
 public java.time.LocalDateTime manuallyConfirmedSentAt;
 public String status;
 public String message;

}
