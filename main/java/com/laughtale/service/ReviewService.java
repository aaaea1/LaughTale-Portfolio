package com.laughtale.service;

import com.laughtale.domain.Book;
import com.laughtale.domain.Member;
import com.laughtale.domain.Rental;
import com.laughtale.domain.Review;
import com.laughtale.repository.RentalRepository;
import com.laughtale.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final MemberService memberService;
    private final BookService bookService;
    private final RentalRepository rentalRepository;
    
    // 후기 작성
    public Review createReview(Integer memberId, Integer bookId, Integer rating, String content, Integer parentId) {
        Member member = memberService.getMemberById(memberId);
        Book book = bookService.getBookById(bookId);
        
        Review review = new Review();
        review.setMember(member);
        review.setBook(book);
        review.setContent(content);

        // 1. 대댓글(답글)인 경우
        if (parentId != null) {
            Review parent = reviewRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다."));
            
            review.setParent(parent);
            review.setDepth(parent.getDepth() + 1); // 부모 depth + 1
            review.setRating(null); // 답글은 별점 반영 X
        } 
        // 2. 일반 후기인 경우 (기존 로직 유지)
        else {
            // 대여 이력 확인
            List<Rental> rentalHistory = rentalRepository.findByMemberAndBook(member, book);
            if (rentalHistory.isEmpty()) {
                throw new IllegalStateException("대여한 적이 없는 도서에는 후기를 작성할 수 없습니다.");
            }
            
            // 이미 작성한 후기가 있는지 확인 (일반 후기일 때만 체크)
            reviewRepository.findByMemberAndBook(member, book).ifPresent(r -> {
                // 부모가 없는(일반 후기) 것들 중에서만 중복 체크를 하려면 추가 로직 필요하지만, 
                // 보통 일반 후기는 도서당 1개만 쓰게 제한합니다.
                if (r.getParent() == null) {
                    throw new IllegalStateException("이미 후기를 작성한 도서입니다.");
                }
            });
            
            review.setRating(rating);
            review.setDepth(0);
        }
        
        return reviewRepository.save(review);
    }
    
    // 후기 수정
    public Review updateReview(Integer reviewId, Integer rating, String content) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("후기를 찾을 수 없습니다."));
        
        review.setRating(rating);
        review.setContent(content);
        
        return reviewRepository.save(review);
    }
    
    // 후기 삭제
    public void deleteReview(Integer reviewId) {
        reviewRepository.deleteById(reviewId);
    }
    
    // 도서의 후기 목록
    @Transactional(readOnly = true)
    public Page<Review> getBookReviews(Integer bookId, Pageable pageable) {
        Book book = bookService.getBookById(bookId);
        return reviewRepository.findByBookOrderByCreatedAtDesc(book, pageable);
    }
    
    // 도서의 모든 후기
    @Transactional(readOnly = true)
    public List<Review> getAllBookReviews(Integer bookId) {
        Book book = bookService.getBookById(bookId);
        return reviewRepository.findByBookOrderByCreatedAtDesc(book);
    }
    
    // 회원의 후기 목록
    @Transactional(readOnly = true)
    public Page<Review> getMemberReviews(Integer memberId, Pageable pageable) {
        Member member = memberService.getMemberById(memberId);
        return reviewRepository.findByMemberOrderByCreatedAtDesc(member, pageable);
    }
    
    // 도서의 평균 평점
    @Transactional(readOnly = true)
    public Double getAverageRating(Integer bookId) {
        Book book = bookService.getBookById(bookId);
        Double avgRating = reviewRepository.findAverageRatingByBook(book);
        return avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0.0;
    }
    
    // 도서의 후기 수
    @Transactional(readOnly = true)
    public long getReviewCount(Integer bookId) {
        Book book = bookService.getBookById(bookId);
        return reviewRepository.countByBook(book);
    }
    
    // 평점별 후기 조회
    @Transactional(readOnly = true)
    public Page<Review> getReviewsByRating(Integer bookId, Integer rating, Pageable pageable) {
        Book book = bookService.getBookById(bookId);
        return reviewRepository.findByBookAndRatingOrderByCreatedAtDesc(book, rating, pageable);
    }
    
    // 최근 후기 조회
    @Transactional(readOnly = true)
    public List<Review> getRecentReviews(int limit) {
        return reviewRepository.findRecentReviews(Pageable.ofSize(limit));
    }
}