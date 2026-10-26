package com.baraka.library.repository;
import com.baraka.library.entity.Renewal;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
import java.util.*;
public interface RenewalRepository extends JpaRepository<Renewal,Long> {
List<Renewal> findByLoanIdOrderByRenewedAtDesc(Long id);
}