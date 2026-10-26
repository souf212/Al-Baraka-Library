package com.baraka.library.repository;
import com.baraka.library.entity.ReminderLog;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
import java.util.*;
public interface ReminderLogRepository extends JpaRepository<ReminderLog,Long> {
 @Override @EntityGraph(attributePaths={"loan","loan.book","loan.borrower"}) Optional<ReminderLog> findById(Long id);
List<ReminderLog> findByLoanIdOrderByPreparedAtDesc(Long id);
}
