package com.baraka.library;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication @EnableScheduling
public class BarakaLibraryApplication {
 public static void main(String[] args) throws Exception {
  String db=System.getenv().getOrDefault("BARAKA_DB","data/baraka-library.db");
  java.nio.file.Path parent=java.nio.file.Path.of(db).toAbsolutePath().getParent();
  java.nio.file.Files.createDirectories(parent);
  if(com.baraka.library.config.Maintenance.run(args,java.nio.file.Path.of(db)))return;
  com.baraka.library.config.Maintenance.lock(java.nio.file.Path.of(db));
  SpringApplication.run(BarakaLibraryApplication.class,args);
 }
}
