package com.baraka.library.service;
import com.baraka.library.entity.*;
import com.baraka.library.repository.ReminderLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
@Service
public class ReminderService {
 private final LibraryService lib;private final ReminderLogRepository logs;
 public ReminderService(LibraryService l,ReminderLogRepository r){lib=l;logs=r;}
 public ReminderLogRepository logs(){return logs;}
 public List<Loan> eligible(){return lib.loans().findByStatusAndDueDateLessThanEqualOrderByDueDate("ACTIVE",lib.today().plusDays(lib.config().leadDays));}
 public org.springframework.data.domain.Page<Loan> page(int page,int size){return lib.loans().findByStatusAndDueDateLessThanEqualOrderByDueDate("ACTIVE",lib.today().plusDays(lib.config().leadDays),org.springframework.data.domain.PageRequest.of(Math.max(0,page),size));}
 @Scheduled(fixedDelay=900000,initialDelay=60000) public void refresh(){page(0,1);} // Computes only; never sends or opens WhatsApp.
 public String type(Loan l){return l.dueDate.isBefore(lib.today())?"OVERDUE":l.dueDate.equals(lib.today())?"DUE_TODAY":"DUE_SOON";}
 public String message(Loan l){Settings s=lib.config();String template=l.overdue(lib.today())?s.overdueTemplate:s.dueTemplate;
  // Literal single-pass substitution prevents user names from being treated as nested placeholders.
  java.util.regex.Matcher m=java.util.regex.Pattern.compile("\\{(borrowerName|bookTitle|dueDate|dueLabel|libraryName)\\}").matcher(template.replace("\\n","\n"));
  String label=l.dueDate.equals(lib.today())?"اليوم":l.dueDate.equals(lib.today().plusDays(1))?"غدًا":"قريبًا";
  Map<String,String> values=Map.of("borrowerName",l.borrower.fullName,"bookTitle",l.book.title,"dueDate",l.dueDate.toString(),"dueLabel",label,"libraryName",s.libraryName);
  StringBuffer out=new StringBuffer();while(m.find())m.appendReplacement(out,java.util.regex.Matcher.quoteReplacement(values.get(m.group(1))));m.appendTail(out);return out.toString();
 }
 public void validate(Loan l){if(!"ACTIVE".equals(l.status)||l.dueDate.isAfter(lib.today().plusDays(lib.config().leadDays)))throw new IllegalArgumentException("هذه الإعارة غير مؤهلة للتذكير الآن");
  if(!l.borrower.whatsappConsent)throw new IllegalArgumentException("لا توجد موافقة صالحة للتواصل عبر واتساب");
  if(l.borrower.whatsappNumber==null)throw new IllegalArgumentException("رقم واتساب غير مسجل");lib.normalizePhone(l.borrower.whatsappNumber,lib.config().internationalPhones);
 }
 public String url(String phone,String message){return "https://wa.me/"+phone.substring(1)+"?text="+URLEncoder.encode(message,StandardCharsets.UTF_8);}
 @Transactional public ReminderLog prepare(long loanId){lib.lock();Loan l=lib.loan(loanId);validate(l);ReminderLog r=new ReminderLog();r.loan=l;r.reminderType=type(l);r.message=message(l);r.status="PREPARED";r.preparedAt=LocalDateTime.now(java.time.ZoneId.of("Africa/Casablanca"));return logs.saveAndFlush(r);}
 @Transactional public String open(long id){lib.lock();ReminderLog r=logs.findById(id).orElseThrow();validate(r.loan);if(r.manuallyConfirmedSentAt!=null)throw new IllegalArgumentException("تم تأكيد إرسال هذا التذكير؛ حضّر تذكيرًا جديدًا عند الحاجة");r.whatsappOpenedAt=LocalDateTime.now(java.time.ZoneId.of("Africa/Casablanca"));r.status="OPENED";logs.saveAndFlush(r);return url(r.loan.borrower.whatsappNumber,r.message);}
 @Transactional public void confirm(long id){lib.lock();ReminderLog r=logs.findById(id).orElseThrow();if(r.whatsappOpenedAt==null)throw new IllegalArgumentException("افتح رابط واتساب أولًا");if(r.manuallyConfirmedSentAt!=null)throw new IllegalArgumentException("تم تسجيل الإرسال بالفعل");r.manuallyConfirmedSentAt=LocalDateTime.now(java.time.ZoneId.of("Africa/Casablanca"));r.status="MANUALLY_CONFIRMED";logs.save(r);}
 public ReminderLog detail(long id){return logs.findById(id).orElseThrow();}
}
