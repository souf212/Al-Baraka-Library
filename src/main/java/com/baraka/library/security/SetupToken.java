package com.baraka.library.security;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import com.baraka.library.repository.AppUserRepository;
import java.security.*;
@Component
public class SetupToken {
 private final String token; private final AppUserRepository users;
 public SetupToken(AppUserRepository u){users=u;byte[] b=new byte[24];new SecureRandom().nextBytes(b);token=java.util.HexFormat.of().formatHex(b);}
 @EventListener(ApplicationReadyEvent.class) public void ready(){if(users.count()==0)System.out.println("FIRST-RUN SETUP TOKEN (local console only): "+token+"\nOpen http://localhost:8080/setup to create your administrator.");}
 public boolean valid(String input){return input!=null&&MessageDigest.isEqual(token.getBytes(java.nio.charset.StandardCharsets.UTF_8),input.getBytes(java.nio.charset.StandardCharsets.UTF_8));}
}
