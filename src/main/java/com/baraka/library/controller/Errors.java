package com.baraka.library.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataAccessException;
@ControllerAdvice
public class Errors {
 @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class) @ResponseStatus(HttpStatus.BAD_REQUEST)
 public String upload(Exception e,Model m){m.addAttribute("message","حجم الصورة أكبر من الحد المسموح: ٢ ميغابايت");return "error";}
 @ExceptionHandler(java.io.IOException.class) @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
 public String files(Exception e,Model m){org.slf4j.LoggerFactory.getLogger(getClass()).warn("File operation failed",e);m.addAttribute("message","تعذر الوصول إلى الملف. تحقق من المساحة المتاحة وصلاحيات مجلد البيانات ثم حاول مجددًا.");return "error";}
 @ExceptionHandler(IllegalArgumentException.class) @ResponseStatus(HttpStatus.BAD_REQUEST)
 public String invalid(IllegalArgumentException e,Model m){m.addAttribute("message",e.getMessage());return "error";}
 @ExceptionHandler({DataAccessException.class,jakarta.persistence.PersistenceException.class}) @ResponseStatus(HttpStatus.CONFLICT)
 public String database(Exception e,Model m){org.slf4j.LoggerFactory.getLogger(getClass()).warn("Database operation failed",e);m.addAttribute("message","تعذر حفظ العملية. قد تكون البيانات تغيرت أو قاعدة البيانات مشغولة. حدّث الصفحة وحاول مجددًا.");return "error";}
 @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class) @ResponseStatus(HttpStatus.BAD_REQUEST)
 public String parameter(Exception e,Model m){m.addAttribute("message","يرجى التحقق من التواريخ والأرقام المدخلة");return "error";}
}
