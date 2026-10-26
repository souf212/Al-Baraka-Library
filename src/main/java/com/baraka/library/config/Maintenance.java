package com.baraka.library.config;
import com.baraka.library.service.BackupService;
import java.nio.file.*;
import java.nio.channels.*;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
/** Runs from the same executable JAR, without starting the web application. */
public final class Maintenance {
 private static FileChannel channel;private static FileLock processLock;
 public static void lock(Path db)throws Exception {
  channel=FileChannel.open(Path.of(db.toAbsolutePath()+".app.lock"),StandardOpenOption.CREATE,StandardOpenOption.WRITE);
  processLock=channel.tryLock();if(processLock==null)throw new IllegalStateException("Application is running. Stop it before restore or starting a second instance.");
 }
 public static boolean run(String[] args,Path db)throws Exception {
  if(args.length==0||(!args[0].equals("--backup")&&!args[0].equals("--restore")))return false;
  Class.forName("org.sqlite.JDBC");
  if(args[0].equals("--backup")&&!Files.isRegularFile(db))throw new IllegalArgumentException("Database not found: "+db);
  if(args[0].equals("--backup")){
   String configured="backups";int keep=14;
   try(var c=DriverManager.getConnection("jdbc:sqlite:"+db.toAbsolutePath());var s=c.createStatement();var r=s.executeQuery("SELECT backup_directory,backup_keep FROM settings WHERE id=1")){if(r.next()){configured=r.getString(1);keep=r.getInt(2);}}
   Path dir=Path.of(System.getenv().getOrDefault("BARAKA_BACKUPS",configured));Files.createDirectories(dir);
   Path dest=dir.resolve("baraka-"+LocalDateTime.now(java.time.ZoneId.of("Africa/Casablanca")).format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))+"-"+java.util.UUID.randomUUID().toString().substring(0,8)+".db").toAbsolutePath();
   vacuum(db,dest);BackupService.validate(dest);
   try(var stream=Files.list(dir)){var files=new java.util.ArrayList<>(stream.filter(p->p.getFileName().toString().matches("baraka-[0-9]{8}-[0-9]{6}-[a-f0-9]{8}\\.db")).map(Path::toAbsolutePath).sorted(java.util.Comparator.comparingLong((Path p)->p.toFile().lastModified()).reversed()).toList());files.remove(dest);files.addFirst(dest);for(int i=keep;i<files.size();i++)Files.delete(files.get(i));}
   System.out.println("Backup validated: "+dest);return true;
  }
  if(args.length!=3||!args[2].equals("--confirm-replace"))throw new IllegalArgumentException("Usage: --restore <backup.db> --confirm-replace. This replaces the current database.");
  Path source=Path.of(args[1]).toAbsolutePath().normalize();if(source.equals(db.toAbsolutePath().normalize()))throw new IllegalArgumentException("Source must differ from active database");
  lock(db);BackupService.validate(source);
  Path staged=Path.of(db.toAbsolutePath()+".restore-"+java.util.UUID.randomUUID());
  // VACUUM reads a consistent snapshot even if the source uses WAL.
  vacuum(source,staged);BackupService.validate(staged);
  Path safety=Path.of(db.toAbsolutePath()+".before-restore-"+System.currentTimeMillis()+".db");
  if(Files.exists(db)){vacuum(db,safety);BackupService.validate(safety);
   try(var c=DriverManager.getConnection("jdbc:sqlite:"+db.toAbsolutePath());var s=c.createStatement()){
   s.execute("PRAGMA busy_timeout=10000");
   try(var r=s.executeQuery("PRAGMA wal_checkpoint(TRUNCATE)")){if(r.next()&&r.getInt(1)!=0)throw new IllegalStateException("Database busy; restore cancelled");}
   }
  }
  Files.deleteIfExists(Path.of(db.toAbsolutePath()+"-wal"));Files.deleteIfExists(Path.of(db.toAbsolutePath()+"-shm"));
  try{Files.move(staged,db.toAbsolutePath(),StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException e){Files.move(staged,db.toAbsolutePath(),StandardCopyOption.REPLACE_EXISTING);}
  System.out.println("Restore validated and complete. Previous database preserved: "+safety);return true;
 }
 private static void vacuum(Path source,Path dest)throws Exception {try(var c=DriverManager.getConnection("jdbc:sqlite:"+source.toAbsolutePath());var s=c.createStatement()){s.execute("PRAGMA busy_timeout=10000");s.execute("VACUUM INTO '"+dest.toString().replace("'","''")+"'");}}
}
