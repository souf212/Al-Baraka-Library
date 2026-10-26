package com.baraka.library.service;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.nio.file.*;
@Service
public class LogoStorage {
 public Path store(MultipartFile file)throws Exception {
  if(file.isEmpty()||file.getSize()>2*1024*1024)throw new IllegalArgumentException("اختر صورة PNG أو JPEG لا تتجاوز ٢ ميغابايت");
  try(var input=ImageIO.createImageInputStream(file.getInputStream())){
   var readers=ImageIO.getImageReaders(input);if(!readers.hasNext())throw new IllegalArgumentException("ملف الصورة غير صالح");
   var reader=readers.next();
   try{reader.setInput(input,true,true);String format=reader.getFormatName();
    if(!format.equalsIgnoreCase("PNG")&&!format.equalsIgnoreCase("JPEG"))throw new IllegalArgumentException("استخدم صورة PNG أو JPEG فقط");
    // Check headers before decoding to prevent highly compressed oversized image allocations.
    if(reader.getWidth(0)>3000||reader.getHeight(0)>3000)throw new IllegalArgumentException("أبعاد الشعار يجب ألا تتجاوز ٣٠٠٠ × ٣٠٠٠");
    var image=reader.read(0);Path dir=Path.of("data/uploads");Files.createDirectories(dir);
    Path temp=Files.createTempFile(dir,"logo-",".png");
    try{ImageIO.write(image,"png",temp.toFile());Path target=dir.resolve("logo.png");Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING);return target;}finally{Files.deleteIfExists(temp);}
   }finally{reader.dispose();}
  }
 }
}
