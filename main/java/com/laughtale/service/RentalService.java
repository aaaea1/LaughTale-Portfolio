package com.laughtale.service;

import com.laughtale.domain.Book;
import com.laughtale.domain.Member;
import com.laughtale.domain.Rental;
import com.laughtale.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RentalService {
    
    private final RentalRepository rentalRepository;
    private final BookService bookService;
    private final MemberService memberService;
    
    private static final int RENTAL_PERIOD_DAYS = 14; // 대여 기간 14일
    
    // 도서 대여
    public Rental rentBook(Integer memberId, Integer bookId) {
        Member member = memberService.getMemberById(memberId);
        Book book = bookService.getBookById(bookId);
        
        // 회원 상태 확인
        if (!"ACTIVE".equals(member.getStatus())) {
            throw new IllegalStateException("利用停止中の会員は貸出できません。");
        }
        
        // 재고 확인
        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("貸出可能な在庫がありません。");
        }
        
        // 이미 대여 중인지 확인
        List<Rental> activeRentals = rentalRepository.findActiveRentalsByMember(member);
        boolean alreadyRented = activeRentals.stream()
                .anyMatch(rental -> rental.getBook().getBookId().equals(bookId));
        
        if (alreadyRented) {
            throw new IllegalStateException("すでに貸出中の書籍です。");
        }
        
        // 대여 처리
        Rental rental = new Rental();
        rental.setMember(member);
        rental.setBook(book);
        rental.setRentalDate(LocalDateTime.now());
        rental.setDueDate(LocalDateTime.now().plusDays(RENTAL_PERIOD_DAYS));
        rental.setStatus("RENTED");
        
        // 재고 감소
        bookService.decreaseAvailableCopies(bookId);
        
        return rentalRepository.save(rental);
    }
    
    // 도서 반납
    public Rental returnBook(Integer rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("貸出情報が見つかりません。"));
        
        if ("RETURNED".equals(rental.getStatus())) {
            throw new IllegalStateException("すでに返却済みの書籍です。");
        }
        
        rental.setReturnDate(LocalDateTime.now());
        rental.setStatus("RETURNED");
        
        // 재고 증가
        bookService.increaseAvailableCopies(rental.getBook().getBookId());
        
        return rentalRepository.save(rental);
    }
    
    // 대여 연장
    public Rental extendRental(Integer rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("貸出情報が見つかりません。"));
        
        if (!"RENTED".equals(rental.getStatus())) {
            throw new IllegalStateException("貸出中の書籍のみ延長できます。");
        }
        
        if (rental.getDueDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("延滞中の書籍は延長できません。");
        }
        
        long currentPeriod = ChronoUnit.DAYS.between(rental.getRentalDate(), rental.getDueDate());

        // 기본 대여 기간이 14일이므로, 이미 14일을 초과(예: 21일)했다면 이미 연장된 상태임
        if (currentPeriod > RENTAL_PERIOD_DAYS) {
            throw new IllegalStateException("これ以上延長できません。");
        }
        
        // 7일 연장
        rental.setDueDate(rental.getDueDate().plusDays(7));
        
        return rentalRepository.save(rental);
    }
    
    // 회원의 대여 이력 조회
    @Transactional(readOnly = true)
    public List<Rental> getRentalHistory(Integer memberId) {
        Member member = memberService.getMemberById(memberId);
        return rentalRepository.findByMemberOrderByRentalDateDesc(member);
    }
    
    // 회원의 대여 이력 조회 (페이징)
    @Transactional(readOnly = true)
    public Page<Rental> getRentalHistoryPaged(Integer memberId, Pageable pageable) {
        Member member = memberService.getMemberById(memberId);
        return rentalRepository.findByMemberOrderByRentalDateDesc(member, pageable);
    }
    
    // 회원의 현재 대여 중인 도서
    @Transactional(readOnly = true)
    public List<Rental> getCurrentRentals(Integer memberId) {
        Member member = memberService.getMemberById(memberId);
        return rentalRepository.findActiveRentalsByMember(member);
    }
    
    // 모든 대여 목록 조회 (관리자)
    @Transactional(readOnly = true)
    public Page<Rental> getAllRentals(Pageable pageable) {
        return rentalRepository.findAll(pageable);
    }
    
    // 상태별 대여 목록 조회
    @Transactional(readOnly = true)
    public Page<Rental> getRentalsByStatus(String status, Pageable pageable) {
        return rentalRepository.findByStatus(status, pageable);
    }
    
    // 연체 도서 자동 처리 (매일 자정 실행)
    @Scheduled(cron = "0 0 0 * * *")
    public void updateOverdueRentals() {
        List<Rental> overdueRentals = rentalRepository.findOverdueRentals(LocalDateTime.now());
        
        for (Rental rental : overdueRentals) {
            rental.setStatus("OVERDUE");
            long overdueDays = ChronoUnit.DAYS.between(rental.getDueDate(), LocalDateTime.now());
            rental.setOverdueDays((int) overdueDays);
            rentalRepository.save(rental);
        }
    }
}