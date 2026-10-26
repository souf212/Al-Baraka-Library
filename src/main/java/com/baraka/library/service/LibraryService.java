package com.baraka.library.service;
import com.baraka.library.entity.*;
import com.baraka.library.dto.Forms.*;
import com.baraka.library.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.*;
import java.time.*;
import java.util.*;
@Service
public class LibraryService {
 private final BookRepository books; private final BorrowerRepository borrowers; private final LoanRepository loans;
 private final SettingsRepository settings; private final AppUserRepository users; private final RenewalRepository renewals;
 private final JdbcTemplate jdbc; private final PasswordEncoder encoder;
 private final org.springframework.security.core.session.SessionRegistry sessions;
 public LibraryService(BookRepository b,BorrowerRepository br,LoanRepository l,SettingsRepository s,AppUserRepository u,RenewalRepository r,JdbcTemplate j,PasswordEncoder e,org.springframework.security.core.session.SessionRegistry sr){books=b;borrowers=br;loans=l;settings=s;users=u;renewals=r;jdbc=j;encoder=e;sessions=sr;}
 public LocalDate today(){return LocalDate.now(ZoneId.of("Africa/Casablanca"));}
 public BookRepository books(){return books;} public BorrowerRepository borrowers(){return borrowers;} public LoanRepository loans(){return loans;}
 public AppUserRepository users(){return users;} public RenewalRepository renewals(){return renewals;} public SettingsRepository settings(){return settings;}
 public Settings config(){return settings.findById(1L).orElseThrow();}
 /** Acquire the SQLite writer before any reads. Serializes writes across threads/processes. */
 public void lock(){jdbc.update("UPDATE write_lock SET value=value WHERE id=1");}
 public Book book(long id){return books.findById(id).orElseThrow(()->new IllegalArgumentException("الكتاب غير موجود"));}
 public Borrower borrower(long id){return borrowers.findById(id).orElseThrow(()->new IllegalArgumentException("المستعير غير موجود"));}
 public Loan loan(long id){return loans.detail(id).orElseThrow(()->new IllegalArgumentException("الإعارة غير موجودة"));}
 public long available(Book b){return b.totalCopies-loans.countByBookIdAndStatus(b.id,"ACTIVE");}
 public long availableTotal(){Long n=jdbc.queryForObject("SELECT COALESCE(SUM(b.total_copies-(SELECT COUNT(*) FROM loans l WHERE l.book_id=b.id AND l.status='ACTIVE')),0) FROM books b WHERE b.active=1",Long.class);return n==null?0:n;}
 public List<Object[]> loansLastReminders(){return jdbc.query("SELECT loan_id,MAX(manually_confirmed_sent_at) FROM reminder_logs WHERE manually_confirmed_sent_at IS NOT NULL GROUP BY loan_id",(r,n)->new Object[]{r.getLong(1),r.getString(2)});}
 public Map<Long,Long> availableFor(List<Book> rows){
  Map<Long,Long> counts=new HashMap<>(); rows.forEach(b->counts.put(b.id,(long)b.totalCopies));
  jdbc.query("SELECT book_id,COUNT(*) AS n FROM loans WHERE status='ACTIVE' GROUP BY book_id",(org.springframework.jdbc.core.RowCallbackHandler)r->{long id=r.getLong(1), n=r.getLong(2); if(counts.containsKey(id)) counts.computeIfPresent(id,(k,v)->v-n);});
  return counts;
 }
 @Transactional public Book saveBook(Long id,BookForm f){lock(); Book b=id==null?new Book():book(id);
  if(f.totalCopies<0 || (id!=null && f.totalCopies<loans.countByBookIdAndStatus(id,"ACTIVE"))) throw new IllegalArgumentException("لا يمكن تقليل النسخ عن عدد النسخ المستعارة");
  String isbn=blank(f.isbn); if(isbn!=null && books.existsByIsbnAndIdNot(isbn,id==null?-1:id)) throw new IllegalArgumentException("الرقم الدولي مسجل لكتاب آخر");
  b.title=f.title.trim();b.author=f.author.trim();b.isbn=isbn;b.category=f.category.trim();b.totalCopies=f.totalCopies;b.description=f.description;b.publisher=f.publisher;b.publicationYear=f.publicationYear;b.shelfLocation=f.shelfLocation;return books.saveAndFlush(b);
 }
 @Transactional public void archive(long id){lock();Book b=book(id);b.active=false;books.save(b);}
 public String normalizePhone(String input,boolean international){
  if(input==null||input.isBlank())return null;
  String n=input.replaceAll("[\\s().-]","");if(n.startsWith("00"))n="+"+n.substring(2);
  if(n.matches("0[5-7][0-9]{8}"))n="+212"+n.substring(1);
  if(n.matches("\\+212[5-7][0-9]{8}"))return n;
  if(international && n.matches("\\+[1-9][0-9]{7,14}"))return n;
  throw new IllegalArgumentException("رقم الهاتف غير صحيح؛ مثال: 0612345678 أو +212612345678");
 }
 @Transactional public Borrower saveBorrower(Long id,BorrowerForm f){lock();Borrower b=id==null?new Borrower():borrower(id);boolean intl=config().internationalPhones;
  b.fullName=f.fullName.trim();b.phoneNumber=normalizePhone(f.phoneNumber,intl);b.whatsappNumber=normalizePhone(f.whatsappNumber,intl);b.email=blank(f.email);b.address=f.address;b.membershipDate=f.membershipDate;b.status=f.status;b.notes=f.notes;b.whatsappConsent=f.whatsappConsent;
  if(b.whatsappConsent && b.whatsappNumber==null)throw new IllegalArgumentException("أدخل رقم واتساب لتسجيل الموافقة");return borrowers.saveAndFlush(b);
 }
 @Transactional public void disableBorrower(long id){lock();Borrower b=borrower(id);b.status="DISABLED";b.whatsappConsent=false;borrowers.save(b);}
 @Transactional public Loan borrow(LoanForm f){lock();Book b=book(f.bookId);Borrower br=borrower(f.borrowerId);
  if(!b.active||available(b)<1)throw new IllegalArgumentException("لا توجد نسخة متاحة من هذا الكتاب");
  if(!"ACTIVE".equals(br.status))throw new IllegalArgumentException("المستعير موقوف عن الاستعارة");
  if(f.loanDate==null||f.dueDate==null||f.loanDate.isAfter(today())||!f.dueDate.isAfter(f.loanDate))throw new IllegalArgumentException("تاريخ الإرجاع يجب أن يلي تاريخ الإعارة، والإعارة لا تبدأ في المستقبل");
  Loan l=new Loan();l.book=b;l.borrower=br;l.loanDate=f.loanDate;l.dueDate=f.dueDate;l.notes=f.notes;return loans.saveAndFlush(l);
 }
 @Transactional public void returnLoan(long id){lock();Loan l=loan(id);active(l);l.status="RETURNED";l.returnedAt=LocalDateTime.now(ZoneId.of("Africa/Casablanca"));loans.saveAndFlush(l);}
 @Transactional public void renew(long id,LocalDate due){lock();Loan l=loan(id);active(l);if(l.renewalCount>=config().maxRenewals)throw new IllegalArgumentException("تم بلوغ الحد الأقصى للتجديد");
  if(due==null || !due.isAfter(l.dueDate)||!due.isAfter(today()))throw new IllegalArgumentException("اختر تاريخًا جديدًا بعد الموعد الحالي وبعد اليوم");
  Renewal r=new Renewal();r.loan=l;r.oldDueDate=l.dueDate;r.newDueDate=due;r.renewedAt=LocalDateTime.now(java.time.ZoneId.of("Africa/Casablanca"));renewals.save(r);l.dueDate=due;l.renewalCount++;loans.saveAndFlush(l);
 }
 private void active(Loan l){if(!"ACTIVE".equals(l.status))throw new IllegalArgumentException("تم إرجاع هذه الإعارة بالفعل");}
 @Transactional public AppUser createUser(UserForm f,boolean first){lock();if(first&&users.count()!=0)throw new IllegalArgumentException("تم إعداد المدير بالفعل");
  if(users.findByUsername(f.username).isPresent())throw new IllegalArgumentException("اسم المستخدم مستخدم بالفعل");
  if(f.password==null||f.password.length()<12||f.password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)throw new IllegalArgumentException("كلمة المرور: ١٢ حرفًا على الأقل و٧٢ بايت على الأكثر");
  AppUser u=new AppUser();u.username=f.username;u.fullName=f.fullName;u.passwordHash=encoder.encode(f.password);u.role=first?"ADMIN":f.role;return users.saveAndFlush(u);
 }
 @Transactional public void toggleUser(long id,String current){lock();AppUser u=users.findById(id).orElseThrow();if(u.username.equals(current))throw new IllegalArgumentException("لا يمكنك تعطيل حسابك الحالي");
  if(u.enabled&&u.role.equals("ADMIN")&&users.findAll().stream().filter(x->x.enabled&&x.role.equals("ADMIN")).count()<=1)throw new IllegalArgumentException("يجب إبقاء مدير واحد على الأقل");u.enabled=!u.enabled;users.save(u);expire(u.username);
 }
 @Transactional public void resetPassword(long id,String password){lock();AppUser u=users.findById(id).orElseThrow();if(password==null||password.length()<12||password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)throw new IllegalArgumentException("كلمة المرور: ١٢ حرفًا على الأقل و٧٢ بايت على الأكثر");u.passwordHash=encoder.encode(password);users.save(u);expire(u.username);}
 private void expire(String name){for(Object p:sessions.getAllPrincipals())if(p instanceof org.springframework.security.core.userdetails.UserDetails u&&u.getUsername().equals(name))sessions.getAllSessions(p,false).forEach(org.springframework.security.core.session.SessionInformation::expireNow);}
 @Transactional public void editUser(long id,String fullName,String role){lock();AppUser u=users.findById(id).orElseThrow();if(fullName==null||fullName.isBlank()||fullName.length()>160||(!"ADMIN".equals(role)&&!"LIBRARIAN".equals(role)))throw new IllegalArgumentException("تحقق من الاسم والدور");
  if(u.enabled&&u.role.equals("ADMIN")&&!role.equals("ADMIN")&&users.findAll().stream().filter(x->x.enabled&&x.role.equals("ADMIN")).count()<=1)throw new IllegalArgumentException("يجب إبقاء مدير واحد مفعّل على الأقل");u.fullName=fullName;u.role=role;users.save(u);expire(u.username);
 }
 @Transactional public void saveSettings(SettingsForm f){lock();Settings s=config();s.libraryName=f.libraryName;s.defaultDays=f.defaultDays;s.maxRenewals=f.maxRenewals;s.leadDays=f.leadDays;s.backupKeep=f.backupKeep;s.backupDirectory=f.backupDirectory;s.internationalPhones=f.internationalPhones;s.dueTemplate=f.dueTemplate;s.overdueTemplate=f.overdueTemplate;settings.save(s);}
 @Transactional public void saveLogo(String path){lock();Settings s=config();s.logoPath=path;settings.save(s);}
 public Page<Loan> loanPage(String status,boolean overdue,long borrower,long book,int page){return loans.search(status,overdue,today(),borrower,book,PageRequest.of(Math.max(0,page),20,Sort.by(Sort.Direction.DESC,"id")));}
 public static String blank(String s){return s==null||s.isBlank()?null:s.trim();}
}
