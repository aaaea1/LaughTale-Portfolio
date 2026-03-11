package com.laughtale.repository;

import com.laughtale.domain.Book;
import com.laughtale.domain.Member;
import com.laughtale.domain.Rental;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Integer> {
    
    // 회원별 대여 이력 조회
    List<Rental> findByMemberOrderByRentalDateDesc(Member member);
    
    Page<Rental> findByMemberOrderByRentalDateDesc(Member member, Pageable pageable);
    
    // 상태별 대여 조회
    Page<Rental> findByStatus(String status, Pageable pageable);
    
    // 회원의 현재 대여 중인 도서
    @Query("SELECT r FROM Rental r WHERE r.member = :member AND r.status IN ('RENTED', 'OVERDUE')")
    List<Rental> findActiveRentalsByMember(@Param("member") Member member);
    
    // 도서의 현재 대여 중인 건수
    @Query("SELECT COUNT(r) FROM Rental r WHERE r.book = :book AND r.status IN ('RENTED', 'OVERDUE')")
    long countActiveRentalsByBook(@Param("book") Book book);
    
    // 연체된 대여 목록
    @Query("SELECT r FROM Rental r WHERE r.status = 'RENTED' AND r.dueDate < :currentDate")
    List<Rental> findOverdueRentals(@Param("currentDate") LocalDateTime currentDate);
    
    // 회원의 특정 도서 대여 이력 확인
    @Query("SELECT r FROM Rental r WHERE r.member = :member AND r.book = :book ORDER BY r.rentalDate DESC")
    List<Rental> findByMemberAndBook(@Param("member") Member member, @Param("book") Book book);
    
    // 반납 예정일이 임박한 대여 (3일 이내)
    @Query("SELECT r FROM Rental r WHERE r.status = 'RENTED' AND r.dueDate BETWEEN :now AND :threeDaysLater")
    List<Rental> findUpcomingDueRentals(@Param("now") LocalDateTime now, @Param("threeDaysLater") LocalDateTime threeDaysLater);
    
    // 회원별 총 대여 횟수
    @Query("SELECT COUNT(r) FROM Rental r WHERE r.member = :member")
    long countByMember(@Param("member") Member member);
}