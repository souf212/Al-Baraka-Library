package com.baraka.library.security;
import com.baraka.library.repository.AppUserRepository;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;
@Configuration
public class SecurityConfig {
 @Bean org.springframework.security.core.session.SessionRegistry sessionRegistry(){return new org.springframework.security.core.session.SessionRegistryImpl();}
 @Bean org.springframework.security.web.session.HttpSessionEventPublisher sessionEvents(){return new org.springframework.security.web.session.HttpSessionEventPublisher();}
 @Bean PasswordEncoder passwords(){return new BCryptPasswordEncoder(12);}
 @Bean UserDetailsService accounts(AppUserRepository users){return name->{var u=users.findByUsername(name).orElseThrow(()->new UsernameNotFoundException("بيانات الدخول غير صحيحة"));return User.withUsername(u.username).password(u.passwordHash).roles(u.role).disabled(!u.enabled).build();};}
 @Bean SecurityFilterChain security(HttpSecurity http,JdbcTemplate jdbc,org.springframework.security.core.session.SessionRegistry registry)throws Exception {
  return http.authorizeHttpRequests(a->a.requestMatchers("/css/**","/js/**","/fonts/**","/vendor/**","/login","/setup","/error").permitAll().requestMatchers("/settings/**","/users/**","/backups/**").hasRole("ADMIN").anyRequest().authenticated())
   .formLogin(f->f.loginPage("/login").successHandler((req,res,auth)->{jdbc.update("UPDATE users SET last_login_at=? WHERE username=?",java.time.LocalDateTime.now(java.time.ZoneId.of("Africa/Casablanca")).toString(),auth.getName());res.sendRedirect(req.getContextPath()+"/");}).permitAll())
   .logout(l->l.logoutSuccessUrl("/login?logout"))
   .sessionManagement(s->s.maximumSessions(-1).sessionRegistry(registry).expiredUrl("/login?logout"))
   .headers(h->h.contentSecurityPolicy(c->c.policyDirectives("default-src 'self'; style-src 'self'; script-src 'self'; img-src 'self' data:; font-src 'self'; frame-ancestors 'none'; base-uri 'self'; form-action 'self' https://wa.me")))
   .build();
 }
}
