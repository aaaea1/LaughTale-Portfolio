package com.laughtale.repository;

import com.laughtale.domain.Book;
import com.laughtale.domain.Member;
import com.laughtale.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    
    // 도서별 리뷰 조회
    Page<Review> findByBookOrderByCreatedAtDesc(Book book, Pageable pageable);
    
    List<Review> findByBookOrderByCreatedAtDesc(Book book);
    
    // 회원별 리뷰 조회
    Page<Review> findByMemberOrderByCreatedAtDesc(Member member, Pageable pageable);
    
    // 회원의 특정 도서 리뷰 확인
    Optional<Review> findByMemberAndBook(Member member, Book book);
    
    // 도서의 평균 평점
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book = :book")
    Double findAverageRatingByBook(@Param("book") Book book);
    
    // 도서의 리뷰 수
    @Query("SELECT COUNT(r) FROM Review r WHERE r.book = :book")
    long countByBook(@Param("book") Book book);
    
    // 평점별 리뷰 조회
    Page<Review> findByBookAndRatingOrderByCreatedAtDesc(Book book, Integer rating, Pageable pageable);
    
    // 최근 리뷰 조회
    @Query("SELECT r FROM Review r ORDER BY r.createdAt DESC")
    List<Review> findRecentReviews(Pageable pageable);
}