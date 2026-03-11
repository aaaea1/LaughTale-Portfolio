package com.laughtale.controller;

import com.laughtale.domain.Book;
import com.laughtale.model.PopularBookDto;
import com.laughtale.service.BookService;
import com.laughtale.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class HomeController {

	private final BookService bookService;
	private final StatisticsService statisticsService;

	@GetMapping("/")
	public String home(Model model) {
		// 인기 도서 TOP 10
		List<PopularBookDto> popularBooks = statisticsService.getPopularBooks();
		model.addAttribute("popularBooks", popularBooks);

		// 최신 도서 4개
		Pageable pageable = PageRequest.of(0, 4, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "bookId"));
	    Page<Book> recentBooks = bookService.getAllBooks(pageable);
	    model.addAttribute("recentBooks", recentBooks.getContent());

		// 장르 목록
		List<String> genres = bookService.getAllGenres();
		model.addAttribute("genres", genres);

		return "index";
	}

	@GetMapping("/books")
	public String bookList(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "12") int size,
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "genre", required = false) String genre, Model model) {

		Pageable pageable = PageRequest.of(page, size);
		Page<Book> books;

		if (keyword != null && !keyword.trim().isEmpty()) {
			books = bookService.searchBooks(keyword, pageable);
			model.addAttribute("keyword", keyword);
		} else if (genre != null && !genre.trim().isEmpty()) {
			books = bookService.getBooksByGenre(genre, pageable);
			model.addAttribute("genre", genre);
		} else {
			books = bookService.getAllBooks(pageable);
		}

		model.addAttribute("books", books);
		model.addAttribute("genres", bookService.getAllGenres());

		return "books/list";
	}

	@GetMapping("/books/detail")
	public String bookDetail(@RequestParam(value = "id") Integer id, Model model) {
		Book book = bookService.getBookById(id);
		model.addAttribute("book", book);
		return "books/detail";
	}

	@GetMapping("/access-denied")
	public String accessDenied() {
		return "error/access-denied";
	}
	
	@GetMapping("/api/books/list")
	@ResponseBody
	public Map<String, Object> getBooksList(
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "12") int size,
	        @RequestParam(required = false) String keyword,
	        @RequestParam(required = false) String genre) {
	    
	    Pageable pageable = PageRequest.of(page, size);
	    Page<Book> books;
	    
	    if (keyword != null && !keyword.trim().isEmpty()) {
	        books = bookService.searchBooks(keyword, pageable);
	    } else if (genre != null && !genre.trim().isEmpty()) {
	        books = bookService.getBooksByGenre(genre, pageable);
	    } else {
	        books = bookService.getAllBooks(pageable);
	    }
	    
	    Map<String, Object> result = new HashMap<>();
	    result.put("content", books.getContent());
	    result.put("totalPages", books.getTotalPages());
	    result.put("currentPage", books.getNumber());
	    result.put("totalElements", books.getTotalElements());
	    
	    return result;
	}
	
	
}