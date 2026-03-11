package com.laughtale.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.laughtale.config.CustomUserDetails;
import com.laughtale.domain.Rental;
import com.laughtale.domain.Reservation;
import com.laughtale.domain.Review;
import com.laughtale.model.MonthlyStatisticsDto;
import com.laughtale.service.RecommendationService;
import com.laughtale.service.RentalService;
import com.laughtale.service.ReservationService;
import com.laughtale.service.ReviewService;
import com.laughtale.service.StatisticsService;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final StatisticsService statisticsService;
    private final RentalService rentalService;
    private final ReservationService reservationService;
    private final ReviewService reviewService;
    private final RecommendationService recommendationService;
    
    /**
     * 도서 대여 API
     */
    @PostMapping("/rentals")
    public ResponseEntity<?> rentBook(@RequestBody RentalRequest request,
                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Rental rental = rentalService.rentBook(userDetails.getMemberId(), request.getBookId());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "貸出が完了しました。",
                "rentalId", rental.getRentalId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 도서 반납 API
     */
    @PostMapping("/rentals/{rentalId}/return")
    public ResponseEntity<?> returnBook(@PathVariable("rentalId") Integer rentalId) {
        try {
            rentalService.returnBook(rentalId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "返却が完了しました。"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 대여 연장 API
     */
    @PostMapping("/rentals/{rentalId}/extend")
    public ResponseEntity<?> extendRental(@PathVariable("rentalId") Integer rentalId) {
        try {
            Rental rental = rentalService.extendRental(rentalId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "貸出期間が延長されました。",
                "dueDate", rental.getDueDate()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 도서 예약 API
     */
    @PostMapping("/reservations")
    public ResponseEntity<?> createReservation(@RequestBody ReservationRequest request,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Reservation reservation = reservationService.createReservation(
                userDetails.getMemberId(), 
                request.getBookId()
            );
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "予約が完了しました。",
                "reservationId", reservation.getReservationId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 예약 취소 API
     */
    @PostMapping("/reservations/{reservationId}/cancel")
    public ResponseEntity<?> cancelReservation(@PathVariable("reservationId") Integer reservationId) {
        try {
            reservationService.cancelReservation(reservationId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "予約がキャンセルされました。"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 후기 작성 API
     */
    /**
     * 후기 작성 API
     */
    @PostMapping("/reviews")
    public ResponseEntity<?> createReview(@RequestBody ReviewRequest request,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Review review = reviewService.createReview(
                userDetails.getMemberId(),
                request.getBookId(),
                request.getRating(),
                request.getContent(),
                request.getParentId() // parentId 인자 추가
            );
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "レビューが投稿されました。",
                "reviewId", review.getReviewId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 후기 수정 API
     */
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<?> updateReview(@PathVariable("reviewId") Integer reviewId,
                                         @RequestBody ReviewRequest request) {
        try {
            	reviewService.updateReview(
                reviewId,
                request.getRating(),
                request.getContent()
            );
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "レビューが更新されました。"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 후기 삭제 API
     */
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable("reviewId") Integer reviewId) {
        try {
            reviewService.deleteReview(reviewId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "レビューが削除されました。"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * AI 추천 새로고침 API
     */
    @GetMapping("/recommendations/refresh")
    public ResponseEntity<?> refreshRecommendations(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            String recommendations = recommendationService.getRecommendations(userDetails.getMemberId());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "recommendations", recommendations
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 도서 평균 평점 조회 API
     */
    @GetMapping("/books/{bookId}/rating")
    public ResponseEntity<?> getAverageRating(@PathVariable("bookId") Integer bookId) {
        try {
            Double avgRating = reviewService.getAverageRating(bookId);
            long reviewCount = reviewService.getReviewCount(bookId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "averageRating", avgRating,
                "reviewCount", reviewCount
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 도서 예약 대기 인원 조회 API
     */
    @GetMapping("/books/{bookId}/waiting-count")
    public ResponseEntity<?> getWaitingCount(@PathVariable("bookId") Integer bookId) {
        try {
            long waitingCount = reservationService.getWaitingCount(bookId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "waitingCount", waitingCount
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/admin/statistics/monthly")
    @ResponseBody 
    public List<MonthlyStatisticsDto> getMonthlyStats(@RequestParam(value = "year", required = false) Integer year) {
        if (year == null) {
            year = java.time.LocalDate.now().getYear();
        }
       return statisticsService.getMonthlyStatistics(year);
    }
    
    /**
     * 특정 도서의 모든 리뷰 목록 조회 API
     */
    @GetMapping("/books/{bookId}/reviews")
    public List<Map<String, Object>> getBookReviews(@PathVariable("bookId") Integer bookId) {
        // 최상위 댓글(parent가 null인 것)만 가져와서 시작하거나, 전체를 가져와서 분류합니다.
        List<Review> reviews = reviewService.getAllBookReviews(bookId);
        
        return reviews.stream()
            .filter(r -> r.getParent() == null) // 최상위 댓글만 먼저 필터링
            .map(this::convertToMap)
            .collect(Collectors.toList());
    }

    private Map<String, Object> convertToMap(Review r) {
        Map<String, Object> map = new HashMap<>();
        map.put("reviewId", r.getReviewId());
        map.put("content", r.getContent());
        map.put("rating", r.getRating());
        map.put("memberName", r.getMember().getName());
        map.put("memberId", r.getMember().getMemberId());
        map.put("createdAt", r.getCreatedAt().toString());
        map.put("depth", r.getDepth());

        // 자식 댓글(대댓글) 재귀적으로 변환
        List<Map<String, Object>> children = r.getChildren().stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
        map.put("children", children);
        
        return map;
    }
    
    // Request DTO
    @Data
    static class RentalRequest {
        private Integer bookId;
    }
    
    @Data
    static class ReservationRequest {
        private Integer bookId;
    }
    
    @Data
    static class ReviewRequest {
        private Integer bookId;
        private Integer rating;
        private String content;
        private Integer parentId;
    }
}