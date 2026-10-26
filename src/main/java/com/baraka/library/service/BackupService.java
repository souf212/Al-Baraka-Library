package com.baraka.library.service;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Value;
import javax.sql.DataSource;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.sql.*;
@Service
public class BackupService {
 private final DataSource ds;private final LibraryService lib;private final String fallback;
 public BackupService(DataSource d,LibraryService l,@Value("${baraka.backup-directory}") String b){ds=d;lib=l;fallback=b;}
 public Path directory(){String configured=lib.config().backupDirectory;return Path.of(configured==null?fallback:configured).toAbsolutePath().normalize();}
 public synchronized Path backup()throws Exception {
  Path dir=directory();Files.createDirectories(dir);Path file=dir.resolve("baraka-"+LocalDateTime.now(java.time.ZoneId.of("Africa/Casablanca")).format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))+"-"+UUID.randomUUID().toString().substring(0,8)+".db");
  // VACUUM INTO produces a transactionally consistent standalone DB, including committed WAL data.
  try(var c=ds.getConnection();var s=c.createStatement()){s.execute("VACUUM INTO '"+file.toString().replace("'","''")+"'");}
  validate(file);var files=new ArrayList<>(list());files.remove(file);files.addFirst(file);for(int i=lib.config().backupKeep;i<files.size();i++)Files.delete(files.get(i));return file;
 }
 public List<Path> list()throws Exception {Path d=directory();if(!Files.exists(d))return List.of();try(var s=Files.list(d)){return s.filter(p->p.getFileName().toString().matches("baraka-[0-9]{8}-[0-9]{6}-[a-f0-9]{8}\\.db")).sorted(Comparator.comparingLong((Path p)->p.toFile().lastModified()).reversed()).toList();}}
 public static void validate(Path p)throws Exception {try(var c=DriverManager.getConnection("jdbc:sqlite:file:"+p.toAbsolutePath().toString().replace('\\','/')+"?mode=ro");var s=c.createStatement()){
  try(var r=s.executeQuery("PRAGMA integrity_check")){if(!r.next()||!"ok".equals(r.getString(1)))throw new IllegalArgumentException("فشل التحقق من سلامة النسخة");}
  try(var r=s.executeQuery("PRAGMA foreign_key_check")){if(r.next())throw new IllegalArgumentException("علاقات النسخة غير سليمة");}
  String sql=new org.springframework.core.io.ClassPathResource("db/migration/V1__initial.sql").getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
  String expected=java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(sql.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
  try(var r=s.executeQuery("SELECT version,checksum FROM schema_version")){if(!r.next()||r.getInt(1)!=1||!expected.equals(r.getString(2))||r.next())throw new IllegalArgumentException("إصدار قاعدة البيانات أو بصمة الترحيل غير متوافق");}
  for(String table:List.of("books","borrowers","loans","users","renewals","reminder_logs","settings","write_lock"))s.executeQuery("SELECT id FROM "+table+" LIMIT 1").close();
 }}
 @Scheduled(cron="0 0 18 * * *",zone="Africa/Casablanca") public void scheduled(){try{backup();}catch(Exception e){org.slf4j.LoggerFactory.getLogger(getClass()).error("Scheduled backup failed: {}",e.getClass().getSimpleName());}}
}
