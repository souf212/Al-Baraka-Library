package com.baraka.library.config;
import jakarta.persistence.*;
import java.time.LocalDate;
@Converter(autoApply=true)
public class LocalDateConverter implements AttributeConverter<LocalDate,String> {
 public String convertToDatabaseColumn(LocalDate value){return value==null?null:value.toString();}
 public LocalDate convertToEntityAttribute(String value){return value==null?null:LocalDate.parse(value);}
}
