package com.baraka.library.entity;
import jakarta.persistence.*;
@Entity @Table(name="books")
public class Book {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public String title;
 public String author;
 public String isbn;
 public String category;
 public String description;
 public String publisher;
 public Integer publicationYear;
 public String shelfLocation;
 public int totalCopies;
 public boolean active = true;
 public java.time.LocalDateTime createdAt;
 public java.time.LocalDateTime updatedAt;
@PrePersist void created(){createdAt=java.time.LocalDateTime.now(java.time.ZoneId.of("Africa/Casablanca"));updatedAt=createdAt;}
@PreUpdate void updated(){updatedAt=java.time.LocalDateTime.now(java.time.ZoneId.of("Africa/Casablanca"));}

}
