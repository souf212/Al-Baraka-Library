package com.baraka.library.config;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Converter(autoApply=true)
public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime,String> {
 public String convertToDatabaseColumn(LocalDateTime value){return value==null?null:value.toString();}
 public LocalDateTime convertToEntityAttribute(String value){return value==null?null:LocalDateTime.parse(value);}
}
