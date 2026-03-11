package com.laughtale.repository;

import com.laughtale.domain.Book;
import com.laughtale.domain.Member;
import com.laughtale.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    
    // 회원별 예약 목록
    List<Reservation> findByMemberOrderByReservationDateDesc(Member member);
    
    // 도서별 예약 목록
    List<Reservation> findByBookAndStatusOrderByReservationDateAsc(Book book, String status);
    
    // 회원의 특정 도서 예약 확인
    @Query("SELECT r FROM Reservation r WHERE r.member = :member AND r.book = :book AND r.status = 'WAITING'")
    Optional<Reservation> findActiveReservation(@Param("member") Member member, @Param("book") Book book);
    
    // 만료된 예약 조회
    @Query("SELECT r FROM Reservation r WHERE r.status = 'WAITING' AND r.expiryDate < :currentDate")
    List<Reservation> findExpiredReservations(@Param("currentDate") LocalDateTime currentDate);
    
    // 도서의 대기 중인 예약 수
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.book = :book AND r.status = 'WAITING'")
    long countWaitingReservationsByBook(@Param("book") Book book);
}