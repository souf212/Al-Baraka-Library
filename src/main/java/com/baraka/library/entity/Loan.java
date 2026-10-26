package com.baraka.library.entity;
import jakarta.persistence.*;
@Entity @Table(name="loans")
public class Loan {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="book_id") Book book;
 public @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="borrower_id") Borrower borrower;
 public java.time.LocalDate loanDate;
 public java.time.LocalDate dueDate;
 public java.time.LocalDateTime returnedAt;
 public String status = "ACTIVE";
 public int renewalCount;
 public String notes;
 public java.time.LocalDateTime createdAt;
 public java.time.LocalDateTime updatedAt;
@PrePersist void created(){createdAt=java.time.LocalDateTime.now(java.time.ZoneId.of("Africa/Casablanca"));updatedAt=createdAt;}
@PreUpdate void updated(){updatedAt=java.time.LocalDateTime.now(java.time.ZoneId.of("Africa/Casablanca"));}
public boolean overdue(java.time.LocalDate today){return status.equals("ACTIVE") && dueDate.isBefore(today);}
}
