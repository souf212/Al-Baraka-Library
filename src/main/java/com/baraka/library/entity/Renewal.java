package com.baraka.library.entity;
import jakarta.persistence.*;
@Entity @Table(name="renewals")
public class Renewal {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="loan_id") Loan loan;
 public java.time.LocalDate oldDueDate;
 public java.time.LocalDate newDueDate;
 public java.time.LocalDateTime renewedAt;

}
