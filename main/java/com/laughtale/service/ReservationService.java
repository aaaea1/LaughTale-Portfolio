package com.laughtale.service;

import com.laughtale.domain.Book;
import com.laughtale.domain.Member;
import com.laughtale.domain.Reservation;
import com.laughtale.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {
    
    private final ReservationRepository reservationRepository;
    private final MemberService memberService;
    private final BookService bookService;
    
    private static final int RESERVATION_EXPIRY_HOURS = 48; // 예약 유효 시간 48시간
    
    // 도서 예약
    public Reservation createReservation(Integer memberId, Integer bookId) {
        Member member = memberService.getMemberById(memberId);
        Book book = bookService.getBookById(bookId);
        
        // 회원 상태 확인
        if (!"ACTIVE".equals(member.getStatus())) {
            throw new IllegalStateException("정지된 회원은 예약할 수 없습니다.");
        }
        
        // 이미 예약 중인지 확인
        reservationRepository.findActiveReservation(member, book).ifPresent(r -> {
            throw new IllegalStateException("이미 예약 중인 도서입니다.");
        });
        
        // 예약 생성
        Reservation reservation = new Reservation();
        reservation.setMember(member);
        reservation.setBook(book);
        reservation.setReservationDate(LocalDateTime.now());
        reservation.setExpiryDate(LocalDateTime.now().plusHours(RESERVATION_EXPIRY_HOURS));
        reservation.setStatus("WAITING");
        
        return reservationRepository.save(reservation);
    }
    
    // 예약 취소
    public void cancelReservation(Integer reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약 정보를 찾을 수 없습니다."));
        
        reservation.setStatus("CANCELLED");
        reservationRepository.save(reservation);
    }
    
    // 예약 완료 처리 (대여 시)
    public void completeReservation(Integer reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약 정보를 찾을 수 없습니다."));
        
        reservation.setStatus("COMPLETED");
        reservationRepository.save(reservation);
    }
    
    // 회원의 예약 목록
    @Transactional(readOnly = true)
    public List<Reservation> getMemberReservations(Integer memberId) {
        Member member = memberService.getMemberById(memberId);
        return reservationRepository.findByMemberOrderByReservationDateDesc(member);
    }
    
    // 도서의 예약 대기 목록
    @Transactional(readOnly = true)
    public List<Reservation> getBookReservations(Integer bookId) {
        Book book = bookService.getBookById(bookId);
        return reservationRepository.findByBookAndStatusOrderByReservationDateAsc(book, "WAITING");
    }
    
    // 도서의 예약 대기 인원 수
    @Transactional(readOnly = true)
    public long getWaitingCount(Integer bookId) {
        Book book = bookService.getBookById(bookId);
        return reservationRepository.countWaitingReservationsByBook(book);
    }
    
    // 만료된 예약 자동 처리 (매 시간 실행)
    @Scheduled(cron = "0 0 * * * *")
    public void cancelExpiredReservations() {
        List<Reservation> expiredReservations = reservationRepository.findExpiredReservations(LocalDateTime.now());
        
        for (Reservation reservation : expiredReservations) {
            reservation.setStatus("EXPIRED");
            reservationRepository.save(reservation);
        }
    }
}
