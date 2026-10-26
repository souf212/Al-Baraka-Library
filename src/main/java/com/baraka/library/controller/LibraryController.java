package com.baraka.library.controller;
import com.baraka.library.dto.Forms.*;
import com.baraka.library.entity.*;
import com.baraka.library.service.*;
import com.baraka.library.security.SetupToken;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.*;
import java.nio.file.*;
import java.util.*;
import java.security.Principal;
@Controller
public class LibraryController {
 private final LibraryService lib; private final ReminderService reminders;private final BackupService backups;private final SetupToken token;private final LogoStorage logos;
 public LibraryController(LibraryService l,ReminderService r,BackupService b,SetupToken t,LogoStorage ls){lib=l;reminders=r;backups=b;token=t;logos=ls;}
 @InitBinder void binding(WebDataBinder b){b.initDirectFieldAccess();}
 @ModelAttribute void common(Model m,Principal p){m.addAttribute("library",lib.config());m.addAttribute("today",lib.today());m.addAttribute("currentUser",p==null?null:lib.users().findByUsername(p.getName()).orElse(null));}
 @GetMapping("/login") String login(){return lib.users().count()==0?"redirect:/setup":"auth/login";}
 @GetMapping("/setup") String setup(Model m){if(lib.users().count()>0)return "redirect:/login";m.addAttribute("form",new UserForm());return "auth/setup";}
 @PostMapping("/setup") String setupSave(@Valid @ModelAttribute("form") UserForm f,BindingResult errors,@RequestParam String setupToken,Model m){
  if(lib.users().count()>0)return "redirect:/login";if(!token.valid(setupToken))errors.reject("token","رمز الإعداد غير صحيح؛ راجع نافذة تشغيل التطبيق");
  if(!errors.hasErrors())try{lib.createUser(f,true);return "redirect:/login?created";}catch(IllegalArgumentException e){errors.reject("form",e.getMessage());}return "auth/setup";
 }
 @GetMapping("/") String dashboard(Model m){m.addAttribute("bookCount",lib.books().count());m.addAttribute("borrowerCount",lib.borrowers().count());long active=lib.loans().countByStatus("ACTIVE");m.addAttribute("activeCount",active);m.addAttribute("availableCount",lib.availableTotal());m.addAttribute("overdueCount",lib.loans().countByStatusAndDueDateBefore("ACTIVE",lib.today()));m.addAttribute("todayCount",lib.loans().countByStatusAndDueDate("ACTIVE",lib.today()));m.addAttribute("recent",lib.loanPage("",false,0,0,0).getContent().stream().limit(6).toList());m.addAttribute("upcoming",reminders.page(0,6).getContent());return "dashboard/index";}
 @GetMapping("/books") String books(@RequestParam(defaultValue="") String q,@RequestParam(defaultValue="") String category,@RequestParam(defaultValue="false") boolean available,@RequestParam(defaultValue="false") boolean archived,@RequestParam(defaultValue="0") int page,Model m){var rows=lib.books().search(q,category,archived,available,PageRequest.of(Math.max(0,page),20,Sort.by(Sort.Direction.DESC,"id")));m.addAttribute("rows",rows);m.addAttribute("availability",lib.availableFor(rows.getContent()));m.addAttribute("categories",lib.books().categories());m.addAttribute("q",q);m.addAttribute("category",category);m.addAttribute("onlyAvailable",available);m.addAttribute("archived",archived);return "books/index";}
 @GetMapping("/books/new") String bookNew(Model m){m.addAttribute("form",new BookForm());return "books/form";}
 @GetMapping("/books/{id}/edit") String bookEdit(@PathVariable long id,Model m){m.addAttribute("form",copy(lib.book(id),new BookForm()));m.addAttribute("id",id);return "books/form";}
 @PostMapping({"/books/new","/books/{id}/edit"}) String bookSave(@PathVariable(required=false) Long id,@Valid @ModelAttribute("form") BookForm f,BindingResult e,Model m,RedirectAttributes flash){if(!e.hasErrors())try{var b=lib.saveBook(id,f);flash.addFlashAttribute("success","تم حفظ الكتاب بنجاح");return "redirect:/books/"+b.id;}catch(IllegalArgumentException ex){e.reject("form",ex.getMessage());}m.addAttribute("id",id);return "books/form";}
 @GetMapping("/books/{id}") String bookDetail(@PathVariable long id,Model m){var b=lib.book(id);m.addAttribute("book",b);m.addAttribute("available",lib.available(b));m.addAttribute("rows",lib.loanPage("",false,0,id,0));return "books/detail";}
 @PostMapping("/books/{id}/archive") String archive(@PathVariable long id,RedirectAttributes f){lib.archive(id);f.addFlashAttribute("success","تمت أرشفة الكتاب مع حفظ السجل");return "redirect:/books";}
 @GetMapping("/borrowers") String borrowers(@RequestParam(defaultValue="") String q,@RequestParam(defaultValue="0") int page,Model m){m.addAttribute("rows",lib.borrowers().search(q,PageRequest.of(Math.max(0,page),20,Sort.by(Sort.Direction.DESC,"id"))));m.addAttribute("q",q);return "borrowers/index";}
 @GetMapping("/borrowers/new") String borrowerNew(Model m){m.addAttribute("form",new BorrowerForm());return "borrowers/form";}
 @GetMapping("/borrowers/{id}/edit") String borrowerEdit(@PathVariable long id,Model m){m.addAttribute("form",copy(lib.borrower(id),new BorrowerForm()));m.addAttribute("id",id);return "borrowers/form";}
 @PostMapping({"/borrowers/new","/borrowers/{id}/edit"}) String borrowerSave(@PathVariable(required=false) Long id,@Valid @ModelAttribute("form") BorrowerForm f,BindingResult e,Model m,RedirectAttributes flash){if(!e.hasErrors())try{var b=lib.saveBorrower(id,f);flash.addFlashAttribute("success","تم حفظ بيانات المستعير");return "redirect:/borrowers/"+b.id;}catch(IllegalArgumentException ex){e.reject("form",ex.getMessage());}m.addAttribute("id",id);return "borrowers/form";}
 @GetMapping("/borrowers/{id}") String borrowerDetail(@PathVariable long id,@RequestParam(defaultValue="0") int page,Model m){m.addAttribute("borrower",lib.borrower(id));m.addAttribute("rows",lib.loanPage("",false,id,0,page));return "borrowers/detail";}
 @PostMapping("/borrowers/{id}/disable") String disable(@PathVariable long id){lib.disableBorrower(id);return "redirect:/borrowers/"+id;}
 @GetMapping({"/loans","/returns"}) String loans(@RequestParam(defaultValue="") String status,@RequestParam(defaultValue="false") boolean overdue,@RequestParam(defaultValue="0") int page,Model m){m.addAttribute("rows",lib.loanPage(status,overdue,0,0,page));m.addAttribute("status",status);m.addAttribute("overdue",overdue);return "loans/index";}
 @GetMapping("/loans/new") String loanNew(Model m){LoanForm f=new LoanForm();f.loanDate=lib.today();f.dueDate=lib.today().plusDays(lib.config().defaultDays);m.addAttribute("form",f);loanChoices(m);return "loans/form";}
 private void loanChoices(Model m){m.addAttribute("books",lib.books().search("","",false,true,PageRequest.of(0,50,Sort.by("title"))).getContent());m.addAttribute("borrowers",lib.borrowers().searchActive("",PageRequest.of(0,50,Sort.by("fullName"))).getContent());}
 @GetMapping("/loans/options/books") @ResponseBody List<Map<String,Object>> bookOptions(@RequestParam(defaultValue="") String q){return lib.books().search(q,"",false,true,PageRequest.of(0,50,Sort.by("title"))).stream().map(b->Map.<String,Object>of("id",b.id,"label",b.title+" — "+b.author)).toList();}
 @GetMapping("/loans/options/borrowers") @ResponseBody List<Map<String,Object>> borrowerOptions(@RequestParam(defaultValue="") String q){return lib.borrowers().searchActive(q,PageRequest.of(0,50,Sort.by("fullName"))).stream().map(b->Map.<String,Object>of("id",b.id,"label",b.fullName)).toList();}
 @PostMapping("/loans/new") String loanSave(@Valid @ModelAttribute("form") LoanForm f,BindingResult e,Model m){if(!e.hasErrors())try{return "redirect:/loans/"+lib.borrow(f).id;}catch(IllegalArgumentException ex){e.reject("form",ex.getMessage());}loanChoices(m);return "loans/form";}
 @GetMapping("/loans/{id}") String loanDetail(@PathVariable long id,Model m){m.addAttribute("loan",lib.loan(id));m.addAttribute("renewals",lib.renewals().findByLoanIdOrderByRenewedAtDesc(id));m.addAttribute("suggestedDue",lib.today().isAfter(lib.loan(id).dueDate)?lib.today().plusDays(lib.config().defaultDays):lib.loan(id).dueDate.plusDays(lib.config().defaultDays));return "loans/detail";}
 @PostMapping("/loans/{id}/return") String returnLoan(@PathVariable long id,RedirectAttributes f){lib.returnLoan(id);f.addFlashAttribute("success","تم تسجيل الإرجاع وأصبحت النسخة متاحة");return "redirect:/loans/"+id;}
 @PostMapping("/loans/{id}/renew") String renew(@PathVariable long id,@RequestParam LocalDate dueDate,RedirectAttributes f){lib.renew(id,dueDate);f.addFlashAttribute("success","تم التجديد وحفظ الموعد السابق");return "redirect:/loans/"+id;}
 @GetMapping("/reminders") String reminders(@RequestParam(defaultValue="0") int page,Model m){var result=reminders.page(page,20);m.addAttribute("rows",result.getContent());m.addAttribute("pagination",result);Map<Long,String> last=new HashMap<>();
  for(Object[] row:lib.loansLastReminders())last.put(((Number)row[0]).longValue(),String.valueOf(row[1]));m.addAttribute("last",last);return "reminders/index";
 }
 @PostMapping("/reminders/prepare/{id}") String prepare(@PathVariable long id){return "redirect:/reminders/"+reminders.prepare(id).id;}
 @GetMapping("/reminders/{id}") String reminder(@PathVariable long id,Model m){m.addAttribute("reminder",reminders.detail(id));return "reminders/detail";}
 @PostMapping("/reminders/{id}/open") ResponseEntity<Void> open(@PathVariable long id){return ResponseEntity.status(303).header(HttpHeaders.LOCATION,reminders.open(id)).build();}
 @PostMapping("/reminders/{id}/confirm") String confirm(@PathVariable long id,RedirectAttributes f){reminders.confirm(id);f.addFlashAttribute("success","تم تسجيل تأكيد الإرسال اليدوي؛ لا يمكن التحقق من التسليم آليًا");return "redirect:/reminders/"+id;}
 @GetMapping("/reports") String reports(Model m){m.addAttribute("popular",lib.loans().popular(PageRequest.of(0,10)));m.addAttribute("activity",lib.loans().activity(PageRequest.of(0,10)));m.addAttribute("total",lib.loans().count());m.addAttribute("active",lib.loans().countByStatus("ACTIVE"));m.addAttribute("overdueCount",lib.loans().countByStatusAndDueDateBefore("ACTIVE",lib.today()));return "reports/index";}
 @GetMapping(value="/reports/export",produces="text/csv;charset=UTF-8") void export(@RequestParam(defaultValue="all") String type,jakarta.servlet.http.HttpServletResponse response)throws Exception {
  response.setHeader("Content-Disposition","attachment; filename=baraka-loans.csv");response.setCharacterEncoding("UTF-8");response.setContentType("text/csv;charset=UTF-8");var out=response.getWriter();out.write('\ufeff');out.write("الكتاب,المستعير,تاريخ الإعارة,تاريخ الإرجاع المتوقع,الحالة,تاريخ الإرجاع الفعلي\r\n");
  int page=0;Page<Loan> rows;do{rows=lib.loanPage(type.equals("active")?"ACTIVE":"",type.equals("overdue"),0,0,page++);for(Loan l:rows){out.write(String.join(",",csv(l.book.title),csv(l.borrower.fullName),csv(l.loanDate.toString()),csv(l.dueDate.toString()),csv(l.status.equals("RETURNED")?"مُرجع":l.overdue(lib.today())?"متأخرة":"نشطة"),csv(l.returnedAt==null?"":l.returnedAt.toString()))+"\r\n");}}while(rows.hasNext());
 }
 public static String csv(String s){if(s==null)s="";String trimmed=s.stripLeading();if(!trimmed.isEmpty()&&("=+-@".indexOf(trimmed.charAt(0))>=0||s.charAt(0)=='\t'||s.charAt(0)=='\r'||s.charAt(0)=='\n'))s="'"+s;return "\""+s.replace("\"","\"\"")+"\"";}
 @GetMapping("/settings") String settings(Model m)throws Exception{m.addAttribute("form",copy(lib.config(),new SettingsForm()));m.addAttribute("backupFiles",backups.list().stream().map(p->p.getFileName().toString()).toList());return "settings/index";}
 @PostMapping("/settings") String settingsSave(@Valid @ModelAttribute("form") SettingsForm f,BindingResult e,Model m,RedirectAttributes flash)throws Exception {if(!e.hasErrors()){lib.saveSettings(f);flash.addFlashAttribute("success","تم حفظ الإعدادات");return "redirect:/settings";}m.addAttribute("backupFiles",backups.list().stream().map(p->p.getFileName().toString()).toList());return "settings/index";}
 @PostMapping("/settings/logo") String logo(@RequestParam MultipartFile file)throws Exception {lib.saveLogo(logos.store(file).toString());return "redirect:/settings";}
 @GetMapping("/logo") ResponseEntity<byte[]> logoGet()throws Exception{String p=lib.config().logoPath;if(p==null)return ResponseEntity.notFound().build();return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(Files.readAllBytes(Path.of(p)));}
 @PostMapping("/backups") String backup(RedirectAttributes f)throws Exception{Path p=backups.backup();f.addFlashAttribute("success","تم إنشاء نسخة سليمة: "+p.getFileName());return "redirect:/settings";}
 @GetMapping("/backups/{name}") ResponseEntity<byte[]> download(@PathVariable String name)throws Exception {Path p=backups.list().stream().filter(x->x.getFileName().toString().equals(name)).findFirst().orElseThrow();return ResponseEntity.ok().header("Content-Disposition","attachment; filename="+name).contentType(MediaType.APPLICATION_OCTET_STREAM).body(Files.readAllBytes(p));}
 @GetMapping("/users") String users(Model m){m.addAttribute("users",lib.users().findAll());m.addAttribute("form",new UserForm());return "settings/users";}
 @PostMapping("/users") String userSave(@Valid @ModelAttribute("form") UserForm f,BindingResult e,Model m){if(!e.hasErrors())try{lib.createUser(f,false);return "redirect:/users";}catch(IllegalArgumentException ex){e.reject("form",ex.getMessage());}m.addAttribute("users",lib.users().findAll());return "settings/users";}
 @PostMapping("/users/{id}/toggle") String toggle(@PathVariable long id,Principal p){lib.toggleUser(id,p.getName());return "redirect:/users";}
 @PostMapping("/users/{id}/password") String password(@PathVariable long id,@RequestParam String password){lib.resetPassword(id,password);return "redirect:/users";}
 @PostMapping("/users/{id}/edit") String userEdit(@PathVariable long id,@RequestParam String fullName,@RequestParam String role){lib.editUser(id,fullName,role);return "redirect:/users";}
 private static <T>T copy(Object from,T to){try{for(var f:to.getClass().getFields())f.set(to,from.getClass().getField(f.getName()).get(from));return to;}catch(ReflectiveOperationException e){throw new IllegalStateException(e);}}
}
