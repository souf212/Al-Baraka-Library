# Complete project structure

Generated build/runtime directories are listed separately below.

```text
albaraka/
├── scripts/
│   ├── backup.bat
│   ├── build.bat
│   ├── restore.bat
│   ├── smoke-test.py
│   ├── start.bat
│   └── write-project-tree.py
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── baraka/
│   │   │           └── library/
│   │   │               ├── config/
│   │   │               │   ├── DatabaseMigration.java
│   │   │               │   ├── LocalDateConverter.java
│   │   │               │   ├── LocalDateTimeConverter.java
│   │   │               │   └── Maintenance.java
│   │   │               ├── controller/
│   │   │               │   ├── Errors.java
│   │   │               │   └── LibraryController.java
│   │   │               ├── dto/
│   │   │               │   └── Forms.java
│   │   │               ├── entity/
│   │   │               │   ├── AppUser.java
│   │   │               │   ├── Book.java
│   │   │               │   ├── Borrower.java
│   │   │               │   ├── Loan.java
│   │   │               │   ├── ReminderLog.java
│   │   │               │   ├── Renewal.java
│   │   │               │   └── Settings.java
│   │   │               ├── repository/
│   │   │               │   ├── AppUserRepository.java
│   │   │               │   ├── BookRepository.java
│   │   │               │   ├── BorrowerRepository.java
│   │   │               │   ├── LoanRepository.java
│   │   │               │   ├── ReminderLogRepository.java
│   │   │               │   ├── RenewalRepository.java
│   │   │               │   └── SettingsRepository.java
│   │   │               ├── security/
│   │   │               │   ├── SecurityConfig.java
│   │   │               │   └── SetupToken.java
│   │   │               ├── service/
│   │   │               │   ├── BackupService.java
│   │   │               │   ├── LibraryService.java
│   │   │               │   ├── LogoStorage.java
│   │   │               │   └── ReminderService.java
│   │   │               └── BarakaLibraryApplication.java
│   │   └── resources/
│   │       ├── db/
│   │       │   └── migration/
│   │       │       └── V1__initial.sql
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   └── app.css
│   │       │   ├── fonts/
│   │       │   │   ├── OFL.txt
│   │       │   │   └── Tajawal-Regular.ttf
│   │       │   ├── js/
│   │       │   │   └── app.js
│   │       │   └── vendor/
│   │       │       ├── BOOTSTRAP-LICENSE
│   │       │       └── bootstrap.rtl.min.css
│   │       ├── templates/
│   │       │   ├── auth/
│   │       │   │   ├── login.html
│   │       │   │   └── setup.html
│   │       │   ├── books/
│   │       │   │   ├── detail.html
│   │       │   │   ├── form.html
│   │       │   │   └── index.html
│   │       │   ├── borrowers/
│   │       │   │   ├── detail.html
│   │       │   │   ├── form.html
│   │       │   │   └── index.html
│   │       │   ├── dashboard/
│   │       │   │   └── index.html
│   │       │   ├── fragments/
│   │       │   │   └── layout.html
│   │       │   ├── loans/
│   │       │   │   ├── detail.html
│   │       │   │   ├── form.html
│   │       │   │   └── index.html
│   │       │   ├── reminders/
│   │       │   │   ├── detail.html
│   │       │   │   └── index.html
│   │       │   ├── reports/
│   │       │   │   └── index.html
│   │       │   ├── settings/
│   │       │   │   ├── index.html
│   │       │   │   └── users.html
│   │       │   └── error.html
│   │       ├── application.yml
│   │       └── messages.properties
│   └── test/
│       └── java/
│           └── com/
│               └── baraka/
│                   └── library/
│                       └── LibraryIntegrationTest.java
├── .env.example
├── .gitignore
├── README.md
├── VERIFICATION.md
├── fetch-assets.py
├── pom.xml
└── tools-bootstrap.py
```

Generated / local-only:

- `target/baraka-library.jar` — executable application.
- `target/surefire-reports/` — automated test evidence.
- `data/` — live database and optional uploaded logo.
- `backups/` — timestamped standalone SQLite backups.
- `.tools/` — optional portable Java/Maven, dependency cache and isolated smoke-test databases.
- `build.log` — latest build output.
- `SMOKE_RESULTS.json` — latest developer runtime smoke-test result.
- `PROJECT_TREE.md` — this file.
