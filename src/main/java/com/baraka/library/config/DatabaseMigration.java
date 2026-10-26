package com.baraka.library.config;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
/** Small SQLite-specific versioned migration runner; handles trigger bodies without splitting semicolons. */
@Configuration
public class DatabaseMigration {
 @Bean HibernatePropertiesCustomizer migrations(DataSource ds,@org.springframework.beans.factory.annotation.Value("${baraka.backup-directory}") String backupDir) throws Exception {
  try(var c=ds.getConnection();var s=c.createStatement()) {
   s.execute("CREATE TABLE IF NOT EXISTS schema_version(version INTEGER PRIMARY KEY, installed_at TEXT NOT NULL, checksum TEXT NOT NULL)");
   String sql=new ClassPathResource("db/migration/V1__initial.sql").getContentAsString(StandardCharsets.UTF_8);
   String checksum=java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(sql.getBytes(StandardCharsets.UTF_8)));
   try(var r=s.executeQuery("SELECT version,checksum FROM schema_version")) { if(r.next()){if(r.getInt(1)!=1||!checksum.equals(r.getString(2))||r.next())throw new IllegalStateException("Database migration version/checksum mismatch; restore a compatible backup or use the correct application version.");return p->{};} }
   c.setAutoCommit(false);
   try { for(String line:sql.lines().filter(l->!l.isBlank()).toList()) s.execute(line);
    try(var p=c.prepareStatement("INSERT INTO schema_version VALUES(1,datetime('now'),?)")){p.setString(1,checksum);p.executeUpdate();}
    try(var p=c.prepareStatement("UPDATE settings SET backup_directory=? WHERE id=1")){p.setString(1,backupDir);p.executeUpdate();}c.commit();
   } catch(Exception e) {c.rollback();throw e;}
  }
  return p->{};
 }
}
