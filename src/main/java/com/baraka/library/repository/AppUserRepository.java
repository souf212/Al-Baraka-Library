package com.baraka.library.repository;
import com.baraka.library.entity.AppUser;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
import java.util.*;
public interface AppUserRepository extends JpaRepository<AppUser,Long> {
Optional<AppUser> findByUsername(String username);
}