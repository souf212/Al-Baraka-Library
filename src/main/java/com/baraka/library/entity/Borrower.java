package com.baraka.library.entity;
import jakarta.persistence.*;
@Entity @Table(name="borrowers")
public class Borrower {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public String fullName;
 public String phoneNumber;
 public String whatsappNumber;
 public String email;
 public String address;
 public java.time.LocalDate membershipDate;
 public String status = "ACTIVE";
 public String notes;
 public boolean whatsappConsent;
 public java.time.LocalDateTime createdAt;
 public java.time.LocalDateTime updatedAt;
@PrePersist void created(){createdAt=java.time.LocalDateTime.now(java.time.ZoneId.of("Africa/Casablanca"));updatedAt=createdAt;}
@PreUpdate void updated(){updatedAt=java.time.LocalDateTime.now(java.time.ZoneId.of("Africa/Casablanca"));}

}
