package com.laughtale.controller;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.laughtale.config.CustomUserDetails;
import com.laughtale.domain.Member;
import com.laughtale.domain.Rental;
import com.laughtale.domain.Reservation;
import com.laughtale.domain.Review;
import com.laughtale.repository.MemberRepository;
import com.laughtale.service.*;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final RentalService rentalService;
    private final ReservationService reservationService;
    private final ReviewService reviewService;
    private final RecommendationService recommendationService;
    private final MemberService memberService;

    // 1. @RequestParam에 name="page" 추가
    @GetMapping("/rentals")
    public String myRentals(@RequestParam(name = "page", defaultValue = "0") int page,
                           Model model,
                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Rental> currentRentals = rentalService.getCurrentRentals(userDetails.getMemberId());
        model.addAttribute("currentRentals", currentRentals);
        
        Pageable pageable = PageRequest.of(page, 10);
        Page<Rental> rentalHistory = rentalService.getRentalHistoryPaged(userDetails.getMemberId(), pageable);
        model.addAttribute("rentalHistory", rentalHistory);
        return "user/rentals";
    }

    // 2. @PathVariable에 "rentalId" 명시
    @PostMapping("/rentals/{rentalId}/return")
    public String returnBook(@PathVariable("rentalId") Integer rentalId,
                            RedirectAttributes redirectAttributes) {
        try {
            rentalService.returnBook(rentalId);
            redirectAttributes.addFlashAttribute("message", "返却が完了しました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user/rentals";
    }
    
    

    @PostMapping("/rentals/{rentalId}/extend")
    public String extendRental(@PathVariable("rentalId") Integer rentalId,
                              RedirectAttributes redirectAttributes) {
        try {
            rentalService.extendRental(rentalId);
            redirectAttributes.addFlashAttribute("message", "貸出期間が延長されました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user/rentals";
    }

    @GetMapping("/reservations")
    public String myReservations(Model model,
                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Reservation> reservations = reservationService.getMemberReservations(userDetails.getMemberId());
        model.addAttribute("reservations", reservations);
        return "user/reservations";
    }

    @PostMapping("/reservations/{reservationId}/cancel")
    public String cancelReservation(@PathVariable("reservationId") Integer reservationId,
                                   RedirectAttributes redirectAttributes) {
        try {
            reservationService.cancelReservation(reservationId);
            redirectAttributes.addFlashAttribute("message", "予約がキャンセルされました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user/reservations";
    }

    @GetMapping("/reviews")
    public String myReviews(@RequestParam(name = "page", defaultValue = "0") int page,
                           Model model,
                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Review> reviews = reviewService.getMemberReviews(userDetails.getMemberId(), pageable);
        model.addAttribute("reviews", reviews);
        return "user/reviews";
    }

    @PostMapping("/reviews/{reviewId}")
    public String updateReview(@PathVariable("reviewId") Integer reviewId,
                              @RequestParam(name = "rating") Integer rating,
                              @RequestParam(name = "content") String content,
                              RedirectAttributes redirectAttributes) {
        try {
            reviewService.updateReview(reviewId, rating, content);
            redirectAttributes.addFlashAttribute("message", "レビューが更新されました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user/reviews";
    }

    @PostMapping("/reviews/{reviewId}/delete")
    public String deleteReview(@PathVariable("reviewId") Integer reviewId,
                              RedirectAttributes redirectAttributes) {
        try {
            reviewService.deleteReview(reviewId);
            redirectAttributes.addFlashAttribute("message", "レビューが更新されました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user/reviews";
    }

    @GetMapping("/recommendations")
    public String recommendations(Model model,
                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        String recommendations = recommendationService.getRecommendations(userDetails.getMemberId());
        model.addAttribute("recommendations", recommendations);
        model.addAttribute("memberId", userDetails.getMemberId());
        return "user/recommendations";
    }
    
    @GetMapping("/profile/data")
    @ResponseBody
    public Member getProfileData(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return memberService.getMemberById(userDetails.getMemberId());
    }

    @GetMapping("/profile")
    public String profile(Model model,
                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        model.addAttribute("member", memberService.getMemberById(userDetails.getMemberId()));
        return "user/profile";
    }

    @PostMapping("/profile")
    @ResponseBody
    public ResponseEntity<String> updateProfile(
    		@RequestParam(name = "name") String name,      
            @RequestParam(name = "phone") String phone,    
            @RequestParam(name = "password", required = false) String password,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        try {
            Member member = memberService.getMemberById(userDetails.getMemberId());
            member.setName(name);
            member.setPhone(phone);
            
            // 비밀번호가 입력된 경우에만 암호화해서 변경
            if (password != null && !password.isEmpty()) {
                member.setPassword(passwordEncoder.encode(password));
            }
            
            memberService.updateMember(userDetails.getMemberId(), member);
            
            userDetails.getMember().setName(name);
            
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
    // 비밀번호 변경 
    @PostMapping("/change-password")
    public String changePassword(@RequestParam(name = "oldPassword") String oldPassword, 
                                 @RequestParam(name = "newPassword") String newPassword,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        try {
            memberService.changePassword(userDetails.getMemberId(), oldPassword, newPassword);
            redirectAttributes.addFlashAttribute("message", "パスワードが変更されました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user/profile";
    }
}