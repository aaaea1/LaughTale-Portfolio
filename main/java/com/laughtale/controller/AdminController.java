package com.laughtale.controller;

import com.laughtale.domain.Book;
import com.laughtale.domain.Member;
import com.laughtale.model.DashboardDto;
import com.laughtale.model.MonthlyStatisticsDto;
import com.laughtale.model.OverdueDto;
import com.laughtale.model.PopularBookDto;
import com.laughtale.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

	private final BookService bookService;
	private final MemberService memberService;
	private final RentalService rentalService;
	private final StatisticsService statisticsService;

	// 관리자 대시보드
	@GetMapping("/dashboard")
	public String dashboard(Model model) {
		// 전체 통계
		DashboardDto dashboard = statisticsService.getDashboardStatistics();
		model.addAttribute("dashboard", dashboard);

		// 인기 도서 TOP 10
		List<PopularBookDto> popularBooks = statisticsService.getPopularBooks();
		model.addAttribute("popularBooks", popularBooks);

		// 연체 목록
		List<OverdueDto> overdueList = statisticsService.getOverdueList();
		model.addAttribute("overdueList", overdueList);

		return "admin/dashboard";
	}

	// 도서 관리 목록
	@GetMapping("/books")
	public String books(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "keyword", required = false) String keyword, Model model) {
		Pageable pageable = PageRequest.of(page, 20);
		Page<Book> books;

		if (keyword != null && !keyword.trim().isEmpty()) {
			books = bookService.searchBooks(keyword, pageable);
			model.addAttribute("keyword", keyword);
		} else {
			books = bookService.getAllBooks(pageable);
		}

		model.addAttribute("books", books);
		model.addAttribute("genres", bookService.getAllGenres());

		return "admin/books";
	}

	// 도서 등록 폼
	@GetMapping("/books/new")
	public String newBookForm(Model model) {
		model.addAttribute("book", new Book());
		model.addAttribute("genres", bookService.getAllGenres());
		return "admin/books-form";
	}

	// 도서 등록 처리
	@PostMapping("/books")
	public String createBook(@ModelAttribute Book book, @RequestParam(value = "file", required = false) MultipartFile file, 
            RedirectAttributes redirectAttributes) {
		try {
			bookService.createBook(book, file);
			redirectAttributes.addFlashAttribute("message", "書籍を登録しました。");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/admin/books";
	}

	// 도서 수정 폼
	@GetMapping("/books/{bookId}/edit")
	public String editBookForm(@PathVariable(value = "bookId") Integer bookId, Model model) {
		Book book = bookService.getBookById(bookId);
		model.addAttribute("book", book);
		model.addAttribute("genres", bookService.getAllGenres());
		return "admin/books-form";
	}

	// 도서 수정 처리
	@PostMapping("/books/{bookId}")
	public String updateBook(@PathVariable(value = "bookId") Integer bookId, 
            @ModelAttribute Book book, 
            @RequestParam(value = "file", required = false) MultipartFile file, 
            RedirectAttributes redirectAttributes) {
		
		try {
			
			Book currentBook = bookService.getBookById(bookId);
			
			bookService.updateBook(bookId, book, file);
			redirectAttributes.addFlashAttribute("message", "書籍情報が更新されました。");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/admin/books";
	}

	// 도서 삭제
	@PostMapping("/books/{bookId}/delete")
	public String deleteBook(@PathVariable(value = "bookId") Integer bookId, RedirectAttributes redirectAttributes) {
		try {
			bookService.deleteBook(bookId);
			redirectAttributes.addFlashAttribute("message", "書籍が削除されました。");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/admin/books";
	}

	// 회원 관리 목록
	@GetMapping("/members")
	public String members(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "keyword", required = false) String keyword, Model model) {
		Pageable pageable = PageRequest.of(page, 20);
		Page<Member> members;

		if (keyword != null && !keyword.trim().isEmpty()) {
			members = memberService.searchMembers(keyword, pageable);
			model.addAttribute("keyword", keyword);
		} else {
			members = memberService.getAllMembers(pageable);
		}

		model.addAttribute("members", members);

		return "admin/members";
	}

	// 회원 상태 변경
	@PostMapping("/members/{memberId}/status")
	public String updateMemberStatus(@PathVariable(value = "memberId") Integer memberId,
            @RequestParam(value = "status") String status, 
            RedirectAttributes redirectAttributes) {
		try {
			memberService.updateMemberStatus(memberId, status);
			redirectAttributes.addFlashAttribute("message", "会員ステータスが変更されました。");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/admin/members";
	}

	// 회원 삭제
	@PostMapping("/members/{memberId}/delete")
	public String deleteMember(@PathVariable(value = "memberId") Integer memberId, RedirectAttributes redirectAttributes) {
		try {
			memberService.deleteMember(memberId);
			redirectAttributes.addFlashAttribute("message", "会員が削除されました。");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/admin/members";
	}

	// 대여 관리
	@GetMapping("/rentals")
	public String rentals(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "status", required = false) String status, Model model) {
		Pageable pageable = PageRequest.of(page, 20);
		Page<com.laughtale.domain.Rental> rentals;

		if (status != null && !status.trim().isEmpty()) {
			rentals = rentalService.getRentalsByStatus(status, pageable);
			model.addAttribute("status", status);
		} else {
			rentals = rentalService.getAllRentals(pageable);
		}

		model.addAttribute("rentals", rentals);

		return "admin/rentals";
	}

	// 통계 페이지
	@GetMapping("/statistics")
	public String statistics(@RequestParam(value = "year", required = false) Integer year, Model model) {
		if (year == null) {
			year = LocalDate.now().getYear();
		}

		// 월별 통계
		List<MonthlyStatisticsDto> monthlyStats = statisticsService.getMonthlyStatistics(year);
		model.addAttribute("monthlyStats", monthlyStats);
		model.addAttribute("year", year);

		// 인기 도서
		List<PopularBookDto> popularBooks = statisticsService.getPopularBooks();
		model.addAttribute("popularBooks", popularBooks);

		// 전체 통계
		DashboardDto dashboard = statisticsService.getDashboardStatistics();
		model.addAttribute("dashboard", dashboard);

		return "admin/statistics";
	}

	// 연체 관리
	@GetMapping("/overdue")
	public String overdue(Model model) {
		List<OverdueDto> overdueList = statisticsService.getOverdueList();
		model.addAttribute("overdueList", overdueList);
		return "admin/overdue";
	}
}