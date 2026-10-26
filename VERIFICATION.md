# Verification record

Verified on 26 October 2026 in the Windows project workspace. No real library database was created or modified during testing. All test accounts, books and loans are in isolated temporary databases.

## Build and automated tests

- Toolchain: Microsoft OpenJDK 21.0.10 and Apache Maven 3.9.11, downloaded into the ignored `.tools/` directory.
- Final command: `mvn -B -Dmaven.repo.local=.tools/m2 verify` (using the portable Maven executable and JAVA_HOME). A clean build was also exercised during delivery.
- Result: **BUILD SUCCESS**.
- JUnit/Spring Boot integration tests: **31 run, 0 failures, 0 errors, 0 skipped**.
- Test suite: `src/test/java/com/baraka/library/LibraryIntegrationTest.java`.
- Machine-readable test evidence: `target/surefire-reports/TEST-com.baraka.library.LibraryIntegrationTest.xml`.
- Executable artifact: `target/baraka-library.jar` (73,445,745 bytes in this build).

The suite covers creation and validation, duplicate ISBN, derived availability, Moroccan/international phone normalization, unavailable/archived books, disabled borrowers, invalid dates, duplicate returns, renewal history/limits, dynamic overdue state, consent revalidation, reminder eligibility, Arabic URL encoding, independent-connection persistence, four simultaneous borrowing attempts for one copy, live backup validation, formula-safe CSV, login, CSRF, librarian authorization, real-data rendering of the application pages, form submission, first-run token rejection, last-administrator protection, backup checksum rejection, retention of the just-created backup, database capacity trigger, invalid logo rejection, and tomorrow/today message wording.

## Packaged JAR over HTTP

`python scripts/smoke-test.py` launches the actual executable on `127.0.0.1:18080` with an isolated `.tools/smoke-*/library.db`. It passed these checks:

1. Arabic first-run page, console-token administrator creation, and login.
2. Book, borrower and loan creation through real HTTP forms with CSRF/session cookies.
3. Bundled Bootstrap RTL, Tajawal, application CSS and JavaScript return HTTP 200 with nonempty contents.
4. SQLite-safe CLI backup while the web application is using WAL.
5. Restore is rejected while the application's file lock is held.
6. Records and login credentials remain valid after stopping and restarting the process.
7. Restore without explicit confirmation is rejected.
8. Confirmed offline restore replaces the returned-loan state with the earlier active-loan snapshot; the restored JAR starts and serves the expected records.

Test processes were stopped afterward. Runtime smoke tests deliberately use isolated data and terminate/restart the test process; normal manager use should stop the application with Ctrl+C.

## Memory sample

The final smoke run sampled a **331.3 MB Windows working set** after startup and a small HTTP workflow, with `-Xms64m -Xmx256m`. This includes JVM/native overhead beyond the Java heap. It is one small-data measurement, not a capacity benchmark or guarantee. Larger data, exports, image processing and concurrent users can require more memory.

## Verification limits

- Server-rendered templates and form workflows were tested, but there was no browser/device visual inspection on an actual desktop, iPhone or Android. The CSS contains responsive sidebar, grid and table behavior; review it on the association's devices before daily use.
- No WhatsApp message was sent. Consent, link generation/encoding, open logging and manual-confirmation rules were tested locally. Actual recipient selection and sending must be checked manually in the manager's WhatsApp account.
- LAN firewall rules, HTTPS certificates and Windows Task Scheduler are documented but were not changed on this machine.
- Backup consistency and restore were exercised on small test databases; storage loss, physical corruption and very large datasets were not fault-tested.
- Optional book-cover upload is omitted. Logo files need separate copying when moving to another laptop; database backups contain only SQLite data.
- No cloud hosting or paid messaging service was used.

Reproduce with `scripts/build.bat` and, optionally, `python scripts/smoke-test.py`. The latter requires Python only for developer verification; Python is not needed to run, back up or restore the library itself.
