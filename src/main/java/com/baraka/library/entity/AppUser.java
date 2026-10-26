package com.baraka.library.entity;
import jakarta.persistence.*;
@Entity @Table(name="users")
public class AppUser {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public String username;
 public String passwordHash;
 public String fullName;
 public String role;
 public boolean enabled = true;
 public java.time.LocalDateTime lastLoginAt;
 public java.time.LocalDateTime createdAt;
@PrePersist void created(){createdAt=java.time.LocalDateTime.now(java.time.ZoneId.of("Africa/Casablanca"));}

}
