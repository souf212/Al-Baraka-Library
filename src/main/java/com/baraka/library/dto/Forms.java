package com.baraka.library.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public class Forms {
 public static class BookForm {
  @NotBlank(message="عنوان الكتاب مطلوب") @Size(max=240,message="العنوان طويل جدًا") public String title;
  @NotBlank(message="اسم المؤلف مطلوب") @Size(max=160,message="اسم المؤلف طويل جدًا") public String author;
  @Size(max=40,message="الرقم الدولي طويل جدًا") public String isbn;
  @NotBlank(message="التصنيف مطلوب") @Size(max=100,message="التصنيف طويل جدًا") public String category;
  @Size(max=4000,message="الوصف طويل جدًا") public String description;
  @Size(max=160,message="اسم الناشر طويل جدًا") public String publisher;
  @Min(value=1,message="سنة النشر غير صحيحة") @Max(value=2200,message="سنة النشر غير صحيحة") public Integer publicationYear;
  @Size(max=100,message="موقع الرف طويل جدًا") public String shelfLocation;
  @Min(value=0,message="عدد النسخ لا يمكن أن يكون سالبًا") @Max(value=100000,message="عدد النسخ كبير جدًا") public int totalCopies=1;
 }
 public static class BorrowerForm {
  @NotBlank(message="الاسم الكامل مطلوب") @Size(max=160,message="الاسم طويل جدًا") public String fullName;
  @NotBlank(message="رقم الهاتف مطلوب") public String phoneNumber;
  public String whatsappNumber;
  @Email(message="البريد الإلكتروني غير صحيح") @Size(max=240,message="البريد طويل جدًا") public String email;
  @Size(max=500,message="العنوان طويل جدًا") public String address;
  @NotNull(message="تاريخ التسجيل مطلوب") public LocalDate membershipDate=LocalDate.now(java.time.ZoneId.of("Africa/Casablanca"));
  @Pattern(regexp="ACTIVE|DISABLED",message="الحالة غير صحيحة") public String status="ACTIVE";
  @Size(max=4000,message="الملاحظات طويلة جدًا") public String notes;
  public boolean whatsappConsent;
 }
 public static class LoanForm {
  @NotNull(message="اختر الكتاب") public Long bookId;
  @NotNull(message="اختر المستعير") public Long borrowerId;
  @NotNull(message="تاريخ الإعارة مطلوب") public LocalDate loanDate;
  @NotNull(message="تاريخ الإرجاع مطلوب") public LocalDate dueDate;
  @Size(max=4000,message="الملاحظات طويلة جدًا") public String notes;
 }
 public static class UserForm {
  @Pattern(regexp="[A-Za-z0-9_.-]{3,40}",message="اسم المستخدم: ٣ إلى ٤٠ حرفًا لاتينيًا أو رقمًا") @NotBlank(message="اسم المستخدم مطلوب") public String username;
  @NotBlank(message="الاسم مطلوب") @Size(max=160,message="الاسم طويل جدًا") public String fullName;
  @NotNull(message="كلمة المرور مطلوبة") @Size(min=12,max=72,message="كلمة المرور بين ١٢ و٧٢ حرفًا") public String password;
  @Pattern(regexp="ADMIN|LIBRARIAN",message="الدور غير صحيح") public String role="LIBRARIAN";
 }
 public static class SettingsForm {
  @NotBlank(message="اسم المكتبة مطلوب") @Size(max=160,message="الاسم طويل جدًا") public String libraryName;
  @Min(1) @Max(365) public int defaultDays;
  @Min(0) @Max(20) public int maxRenewals;
  @Min(1) @Max(30) public int leadDays;
  @Min(1) @Max(365) public int backupKeep;
  @NotBlank(message="مجلد النسخ مطلوب") public String backupDirectory;
  public boolean internationalPhones;
  @NotBlank(message="قالب التذكير مطلوب") @Size(max=4000,message="القالب طويل جدًا") public String dueTemplate;
  @NotBlank(message="قالب التأخير مطلوب") @Size(max=4000,message="القالب طويل جدًا") public String overdueTemplate;
 }
}
