package com.baraka.library.repository;
import com.baraka.library.entity.Loan;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
import java.util.*;
public interface LoanRepository extends JpaRepository<Loan,Long> {
 @EntityGraph(attributePaths={"book","borrower"}) Page<Loan> findByStatusAndDueDateLessThanEqualOrderByDueDate(String status,java.time.LocalDate date,Pageable p);
@EntityGraph(attributePaths={"book","borrower"}) @Query("select l from Loan l where (:status='' or l.status=:status) and (:overdue=false or (l.status='ACTIVE' and l.dueDate<:today)) and (:borrower=0 or l.borrower.id=:borrower) and (:book=0 or l.book.id=:book)") Page<Loan> search(@Param("status") String status,@Param("overdue") boolean overdue,@Param("today") java.time.LocalDate today,@Param("borrower") long borrower,@Param("book") long book,Pageable p);
 long countByBookIdAndStatus(Long id,String status);
 long countByStatus(String status);
 long countByStatusAndDueDateBefore(String status,java.time.LocalDate date);
 long countByStatusAndDueDate(String status,java.time.LocalDate date);
 @EntityGraph(attributePaths={"book","borrower"}) List<Loan> findByStatusAndDueDateLessThanEqualOrderByDueDate(String status,java.time.LocalDate date);
 @EntityGraph(attributePaths={"book","borrower"}) @Query("select l from Loan l where l.id=:id") Optional<Loan> detail(@Param("id") long id);
 @Query("select l.book.title,count(l) from Loan l group by l.book.id,l.book.title order by count(l) desc") List<Object[]> popular(Pageable p);
 @Query("select l.borrower.fullName,count(l) from Loan l group by l.borrower.id,l.borrower.fullName order by count(l) desc") List<Object[]> activity(Pageable p);
}
