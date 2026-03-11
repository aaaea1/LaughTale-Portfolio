package com.laughtale.repository;

import com.laughtale.domain.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {
    
    // 제목으로 검색
    Page<Book> findByTitleContaining(String title, Pageable pageable);
    
    // 작가로 검색
    Page<Book> findByAuthorContaining(String author, Pageable pageable);
    
    // 장르로 검색
    Page<Book> findByGenre(String genre, Pageable pageable);
    
    // 복합 검색 (제목 또는 작가)
    @Query("SELECT b FROM Book b WHERE b.title LIKE %:keyword% OR b.author LIKE %:keyword%")
    Page<Book> searchBooks(@Param("keyword") String keyword, Pageable pageable);
    
    // 대여 가능한 도서만 조회
    @Query("SELECT b FROM Book b WHERE b.availableCopies > 0")
    Page<Book> findAvailableBooks(Pageable pageable);
    
    // 모든 장르 목록 조회
    @Query("SELECT DISTINCT b.genre FROM Book b WHERE b.genre IS NOT NULL ORDER BY b.genre")
    List<String> findAllGenres();
    
    // ISBN으로 조회
    Book findByIsbn(String isbn);
}