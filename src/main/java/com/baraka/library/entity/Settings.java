package com.baraka.library.entity;
import jakarta.persistence.*;
@Entity @Table(name="settings")
public class Settings {
 @Id  public Long id;
 public String libraryName;
 public int defaultDays;
 public int maxRenewals;
 public int leadDays;
 public int backupKeep;
 public String backupDirectory;
 public boolean internationalPhones;
 public String logoPath;
 public String dueTemplate;
 public String overdueTemplate;

}
