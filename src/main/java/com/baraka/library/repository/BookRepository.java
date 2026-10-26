package com.baraka.library.repository;
import com.baraka.library.entity.Book;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
import java.util.*;
public interface BookRepository extends JpaRepository<Book,Long> {
@Query("select b from Book b where (:q='' or lower(b.title) like lower(concat('%',:q,'%')) or lower(b.author) like lower(concat('%',:q,'%')) or b.isbn like concat('%',:q,'%')) and (:category='' or b.category=:category) and (:archived=true or b.active=true) and (:available=false or b.totalCopies > (select count(l) from Loan l where l.book=b and l.status='ACTIVE'))")
 Page<Book> search(@Param("q") String q,@Param("category") String category,@Param("archived") boolean archived,@Param("available") boolean available,Pageable p);
 @Query("select distinct b.category from Book b order by b.category") List<String> categories();
 @Query("select coalesce(sum(b.totalCopies),0) from Book b") long totalCopies();
 boolean existsByIsbnAndIdNot(String isbn,Long id);
}