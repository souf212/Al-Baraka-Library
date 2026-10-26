package com.baraka.library.repository;
import com.baraka.library.entity.Borrower;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
import java.util.*;
public interface BorrowerRepository extends JpaRepository<Borrower,Long> {
 @Query("select b from Borrower b where b.status='ACTIVE' and (:q='' or lower(b.fullName) like lower(concat('%',:q,'%')) or b.phoneNumber like concat('%',:q,'%'))") Page<Borrower> searchActive(@Param("q") String q,Pageable p);
@Query("select b from Borrower b where :q='' or lower(b.fullName) like lower(concat('%',:q,'%')) or b.phoneNumber like concat('%',:q,'%')") Page<Borrower> search(@Param("q") String q,Pageable p);
 List<Borrower> findByStatusOrderByFullName(String status);
}
