# مكتبة البركة — Al Baraka Library

تطبيق محلي لإدارة مكتبة جمعية: الكتب، المستعيرون، الإعارات، الإرجاع، التجديد، والتذكير اليدوي عبر واتساب. واجهة عربية باتجاه RTL، دون اشتراك أو استضافة سحابية.

This is a Java 21 / Spring Boot 3.5.16 modular monolith, using MVC, JPA/Hibernate's matching community SQLite dialect, Spring Security, Thymeleaf, SQLite JDBC, Bootstrap 5.3.8 RTL, Tajawal, and vanilla JavaScript. All frontend assets are included. The JAR needs no internet after installation; WhatsApp requires internet and your manual action.

## Quick start on Windows

1. Install a Java **21 JDK** and Maven 3.6.3 or newer. Confirm with `java -version` and `mvn -version`. The current workspace also contains a downloaded portable toolchain in `.tools/`; the supplied build/start scripts detect it. Use a current Java 21 security update for ongoing deployment.
2. Open a terminal in this project directory. Build:

   ```powershell
   scripts\build.bat
   # Or, with Java and Maven on PATH:
   mvn clean verify
   ```

3. Run `scripts\start.bat`, or:

   ```powershell
   java -Xms64m -Xmx256m -jar target/baraka-library.jar
   ```

4. Open **http://localhost:8080**. Keep the terminal open while using the application.
5. First startup prints `FIRST-RUN SETUP TOKEN (local console only): ...`. Copy that token into the Arabic setup page. Choose your own username, full name, and a password of at least 12 characters (at most 72 UTF-8 bytes, because BCrypt has a byte limit). There is **no default account or password**. The one-time token changes on restart, and setup closes permanently once an account exists.
6. Sign in. Add books, register borrowers and their consent, then choose **إعارة جديدة**. To return or renew, open a loan's details.

Stop using **Ctrl+C** in the terminal. Do not terminate the laptop while a backup/restore is running.

## Daily workflows

- **الكتب:** add/edit all bibliographic fields, search title/author/ISBN, filter category and availability, inspect history, and archive. Archived books keep their history and cannot be newly borrowed. Copies cannot be reduced below outstanding loans. ISBN is optional and unique when provided.
- **المستعيرون:** register/edit readers, contacts, membership dates, status, notes and WhatsApp consent. Disabling a reader prevents new loans and revokes consent. Their history remains available. Restore their active status through the edit form.
- **الإعارات:** select a reader and available book; dates default to today and today + 14 days. Search fields query the database and show up to 50 matching options at a time. Type a more specific name/title when necessary. The server rechecks capacity and reader status when saving.
- **الإرجاع والتجديد:** open the loan, confirm receipt to return it, or enter a later date to renew. Defaults allow 2 renewals; administrators can change the limit. Every renewal records both old and new due dates. Returned loans cannot be returned twice or renewed.
- **التذكيرات:** due soon (default tomorrow), due today, and overdue loans are recalculated when pages open and every 15 minutes while running. Returned loans disappear. No scheduled task opens or sends WhatsApp.
- **التقارير:** totals, active/overdue/history CSV, ten most borrowed books and ten most active borrowers. CSV includes UTF-8 BOM for Excel and quotes/neutralizes formula-leading cells. Exports stream in pages rather than loading the entire history.
- **الإعدادات:** administrators manage borrowing limits, reminder lead time, message templates, library name/logo, backup retention/directory, international phone support and users. Librarians cannot access these routes or backup operations.

Books, borrowers, loans and reminder tables paginate. Availability is derived from loans, never edited separately. Dates and schedules use **Africa/Casablanca**. Overdue means an active loan whose due date is before today; due today is not overdue.

## WhatsApp: what the buttons mean

1. Record the reader's explicit WhatsApp consent and number. Moroccan numbers such as `0612345678`, `00212612345678` and `+212612345678` normalize to `+212612345678`. Other countries require the international-number setting and E.164 input.
2. Choose **تحضير التذكير** to save and preview a personalized Arabic message. Templates support `{borrowerName}`, `{bookTitle}`, `{dueDate}`, `{dueLabel}` (today/tomorrow/soon in Arabic) and `{libraryName}`.
3. Choose **إرسال عبر واتساب**. The server checks consent and eligibility again, logs that the link was opened, and redirects a new tab to `https://wa.me/…?text=…`.
4. In WhatsApp, review the recipient and **press Send yourself**. Opening the link does not send a message.
5. Return to the library, refresh the reminder page, and choose **أؤكد أنني أرسلت الرسالة يدويًا** only if you sent it. Last manually confirmed dates are visible in the reminder list.

The application records PREPARED, OPENED and MANUALLY_CONFIRMED internally. It cannot detect delivery, read receipts, or whether WhatsApp actually opened. No paid API, browser automation, unofficial client or automatic sending is used. Clipboard copying has a fallback for local HTTP; if a browser blocks it, select the message and copy manually.

## Persistent files and configuration

Paths are relative to the working directory. Always launch from this project root (the scripts do this). The JAR can be updated independently of the data.

| File or setting | Default / purpose |
| --- | --- |
| `data/baraka-library.db` | Persistent SQLite database, including users/settings |
| `data/baraka-library.db-wal`, `-shm` | SQLite-managed live WAL files; never copy just the main file while running |
| `data/baraka-library.db.app.lock` | Prevents two instances and restore while this app is running; an empty file after shutdown is normal |
| `data/uploads/logo.png` | Optional re-encoded local logo; copy this directory separately for full machine migration |
| `backups/baraka-YYYYMMDD-HHmmss-xxxxxxxx.db` | Standalone consistent database backups |
| `BARAKA_DB` | Overrides database file path; set before startup, backup and restore |
| `BARAKA_BIND` | `127.0.0.1` for laptop-only access |
| `BARAKA_PORT` | `8080` |
| `BARAKA_BACKUPS` | Initial backup directory; afterward the saved admin setting controls web backups |
| `BARAKA_HTTPS` | `false`; set `true` only when serving HTTPS so session cookies are Secure |

Example:

```powershell
$env:BARAKA_DB = 'D:/Association/baraka-library.db'
$env:BARAKA_BIND = '127.0.0.1'
java -jar target/baraka-library.jar
```

`.env.example` is documentation; Spring does not load `.env` automatically. For persistent environment variables use Windows Environment Variables or set them in your own launch script. Avoid overriding `spring.datasource.url` independently: the process/restore guard uses `BARAKA_DB`.

SQLite uses foreign keys, WAL, a 10-second busy timeout and a four-connection pool. Every service mutation obtains the SQLite writer lock **before reading**, so capacity checks are serialized even across threads. A database trigger also rejects loans above capacity. JPA schema generation is disabled. Transactional, versioned SQL migrations run before Hibernate initializes; this small SQLite runner handles trigger statements and verifies the migration SHA-256 checksum. Flyway is intentionally omitted. Do not edit an applied migration; introduce a new version and extend the migration runner for future releases.

## Backups and safe restore

**Make a backup after important changes and keep another copy off the laptop**, for example on an association USB drive. Backups contain personal data and password hashes; protect them like the laptop account.

Administrators can select **إنشاء نسخة الآن** in settings and download a backup. A daily job runs at **18:00 Casablanca time**, only while the application is running. It keeps the configured number of newest app-created backups (default 14). A missed scheduled run while the laptop is off is not replayed. Create a manual backup after an extended shutdown.

From a terminal:

```powershell
scripts\backup.bat
# Equivalent:
java -jar target/baraka-library.jar --backup
```

Backups use **SQLite VACUUM INTO**, not a live file copy. The result includes committed WAL changes and is checked with integrity and foreign-key checks. The application does not automatically back up the optional logo file; copy `data/uploads/` separately.

Restore:

1. Stop the application with Ctrl+C. Close other SQLite tools using the database. Restore is intentionally performed offline to avoid replacing an open pool's database.
2. Set the same `BARAKA_DB` used for startup, if customized.
3. Run `scripts\restore.bat "backups\baraka-...db"` and type **RESTORE** when prompted. Cancellation leaves the database untouched.
4. The JAR refuses to restore while the application lock is held, checks integrity/schema version/foreign keys, creates and validates a staged snapshot, preserves the previous database as `*.before-restore-<timestamp>.db`, checkpoints WAL, and then replaces the database. Start the application again.
5. Verify counts and a few reader/loan records before resuming normal use. Restored accounts and settings are those in the backup.

For a new laptop, create the project data directory and run restore before first startup; a destination database does not need to exist. The low-level CLI requires an explicit flag:

```powershell
java -jar target/baraka-library.jar --restore 'backups/your-backup.db' --confirm-replace
```

Do not manually delete or replace a running database, and do not operate a second application instance through a different alias/symlink to the same file. The guard coordinates this application; close unrelated database programs yourself before restore.

## Optional phone access on trusted Wi-Fi

The laptop must stay on and the phone must be on the same trusted network. This is local network access, not public hosting.

1. Complete administrator setup on the laptop first.
2. Run `ipconfig`; find the active Wi-Fi adapter's IPv4 address, for example `192.168.1.25`.
3. Restart with `$env:BARAKA_BIND='192.168.1.25'` (or `0.0.0.0` to listen on all interfaces). Prefer the specific trusted interface.
4. In an elevated PowerShell, allow only the local subnet on the **Private** profile:

   ```powershell
   New-NetFirewallRule -DisplayName 'Al Baraka Library LAN' -Direction Inbound -Action Allow -Protocol TCP -LocalPort 8080 -Profile Private -RemoteAddress LocalSubnet
   ```

5. On iPhone/Android, open `http://192.168.1.25:8080` and sign in. Router client isolation may block device-to-device access; check the router settings. The IP may change after reconnecting.
6. Remove the rule when no longer needed: `Remove-NetFirewallRule -DisplayName 'Al Baraka Library LAN'`.

HTTP does **not encrypt passwords, session cookies, or reader data** on the network. Use only a trusted private network and trusted devices. Do not forward this port on your router, expose it publicly, or use public Wi-Fi. Laptop-only loopback is the default.

For optional HTTPS, generate a PKCS12 certificate with the JDK `keytool`, including your LAN IP in its subject alternative names, and configure Spring's `server.ssl.key-store`, `server.ssl.key-store-type=PKCS12`, `server.ssl.key-store-password`, and `server.ssl.key-alias` in an external configuration file kept outside source control. Example certificate command (choose a password interactively):

```powershell
keytool -genkeypair -alias baraka -keyalg RSA -keysize 3072 -storetype PKCS12 -keystore data/baraka.p12 -validity 825 -dname 'CN=Al Baraka Library' -ext 'SAN=ip:192.168.1.25,dns:localhost'
```

Example external `application-local.yml`:

```yaml
server:
  ssl:
    enabled: true
    key-store: file:./data/baraka.p12
    key-store-type: PKCS12
    key-store-password: ${BARAKA_KEYSTORE_PASSWORD}
    key-alias: baraka
```

Set `BARAKA_HTTPS=true` and `BARAKA_KEYSTORE_PASSWORD`, then add `--spring.config.additional-location=file:./application-local.yml` to the launch command. Open `https://<IP>:8080`. A self-signed certificate must be trusted on each device; a managed local CA is better for regular use. Keep the private key and keystore password private.

## Start with Windows

Open Task Scheduler → Create Task. Use your Windows account, **Run only when user is logged on** if you want the console visible, trigger **At log on**, and action `cmd.exe` with arguments `/c "C:\path\to\albaraka\scripts\start.bat"`. Set **Start in** to the project root. Disable forced stop time limits. A logon trigger is simpler for a manager who needs the initial setup token. For a boot trigger without a logged-in session, configure an account with access to the data folder, set the working directory and environment explicitly, and redirect logs to an access-restricted local file. Never run two startup tasks for the same database.

## Tests, packaging and troubleshooting

Run `mvn clean verify`. JUnit/Spring Boot integration tests create an isolated temporary SQLite database and never use `data/baraka-library.db`. Tests exercise validation, availability, registration, loan/return/renewal rules, overdue detection, reminder eligibility/encoding/consent, login/CSRF/roles, persistence, concurrent borrowing, backup consistency, CSV protection, and rendered Arabic pages. Surefire results are in `target/surefire-reports/`. The executable is `target/baraka-library.jar`; deploy it with `scripts/`, keeping `data/` and `backups/` in the same stable working directory.

| Symptom | Action |
| --- | --- |
| Java not found / unsupported class version | Install/select Java 21; verify JAVA_HOME and PATH |
| Maven cannot download dependencies | Initial build needs internet; check proxy/firewall. Runtime does not. Rebuild after resolving connectivity |
| Port 8080 in use | Stop the other instance or set `BARAKA_PORT=8081` |
| Application running / restore refused | Stop the existing application normally. The lock file itself is harmless; deleting it is not the solution |
| Database busy / operation rejected | Wait briefly, refresh, retry; close external SQLite editors. The transaction is rolled back on failure |
| Restoring over a physically corrupt database fails safety validation | Keep the original database and all WAL sidecars untouched. Set BARAKA_DB to a new file path, restore the verified backup there, and use that same new path for startup. This preserves the damaged original for recovery |
| Migration checksum/version mismatch | Use the application version matching the backup; do not alter migrations or erase real data to bypass the check |
| Wrong database appears empty | Confirm working directory and BARAKA_DB before creating records |
| Forbidden / form expired | Sign in with the appropriate role; reload stale forms for a fresh CSRF token |
| Arabic broken in terminal | Terminal encoding may differ; pages/files are UTF-8. Use Windows Terminal / UTF-8; Excel exports have BOM |
| WhatsApp unavailable | Confirm consent, number, internet, and that WhatsApp is installed or its web client is accessible |
| Phone cannot connect | Check laptop IP, binding, Private firewall rule, same Wi-Fi and router isolation |
| Memory pressure | Raise `-Xmx` if needed; 256 MB is a starting heap setting, not a promise of total memory usage |

Passwords use BCrypt cost 12. Security uses sessions (30-minute idle timeout), session fixation protection, HttpOnly/SameSite=Strict cookies, CSRF, role checks and restrictive CSP. Account disabling/password reset expires registered sessions. Backups and user administration are admin-only; restore also needs local filesystem/console access. Protect the Windows account and disk (for example BitLocker). No password recovery email service is assumed: another administrator can reset passwords. If every administrator loses access, restore a known backup or arrange a controlled local recovery; no insecure password bypass is shipped.

## Project layout and verification limits

See [PROJECT_TREE.md](PROJECT_TREE.md) for the complete source tree and [VERIFICATION.md](VERIFICATION.md) for the recorded build/runtime results. Controllers, services, repositories, entities, DTO validation, configuration/converters, and security are separate packages; reusable Thymeleaf fragments keep the Arabic UI consistent.

This delivery has no book-cover uploads (optional in the brief). The association logo supports PNG/JPEG and is re-encoded as PNG, limited to 2 MB and 3000×3000 pixels. Backups are not encrypted and scheduled jobs run only while the app is on. Real-device layout review and actual manual WhatsApp sending require the manager's devices/account. Review dependency updates and test backups periodically before relying on the application for important association data.

Bundled third-party licenses are in `static/vendor/BOOTSTRAP-LICENSE` (MIT) and `static/fonts/OFL.txt` (SIL OFL). Spring Boot release/system requirements: [official documentation](https://docs.spring.io/spring-boot/3.5/system-requirements.html). Hibernate dialects: [official documentation](https://docs.hibernate.org/orm/6.6/dialect/).
