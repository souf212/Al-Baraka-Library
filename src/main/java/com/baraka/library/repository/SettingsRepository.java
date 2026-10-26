package com.baraka.library.repository;
import com.baraka.library.entity.Settings;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
import java.util.*;
public interface SettingsRepository extends JpaRepository<Settings,Long> {

}