package com.laughtale.service;

import com.laughtale.domain.Book;
import com.laughtale.repository.BookRepository;
import com.laughtale.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BookService {
    
    private final BookRepository bookRepository;
    private final RentalRepository rentalRepository;
    private static final String UPLOAD_DIR = "uploads/books/";
    
    // 도서 등록
    public Book createBook(Book book, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            String savedFileName = saveFile(file);
            book.setFileName(file.getOriginalFilename());
            book.setFilePath(UPLOAD_DIR + savedFileName);
        }
        

     // 초기값 설정 (null이거나 0인 경우만 기본값 1 설정)
        if (book.getTotalCopies() == null || book.getTotalCopies() == 0) {
            book.setTotalCopies(1);
        }

        // availableCopies는 totalCopies와 같게 설정 (신규 등록이므로)
        book.setRentalCount(0);
        book.setAvailableCopies(book.getTotalCopies());
        
        return bookRepository.save(book);
    }
    
    // 도서 수정
    public Book updateBook(Integer bookId, Book updatedBook, MultipartFile file) throws IOException {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("書籍が見つかりません。"));
        
        book.setTitle(updatedBook.getTitle());
        book.setAuthor(updatedBook.getAuthor());
        book.setPublisher(updatedBook.getPublisher());
        book.setGenre(updatedBook.getGenre());
        book.setIsbn(updatedBook.getIsbn());
        book.setDescription(updatedBook.getDescription());
        
     // 현재 대여 중인 권수 계산
        long currentlyRented = rentalRepository.countActiveRentalsByBook(book);

        // 총 권수 변경
        Integer newTotalCopies = updatedBook.getTotalCopies();
        if (newTotalCopies != null && newTotalCopies > 0) {
            // 총 권수가 대여 중인 권수보다 작으면 안됨
            if (newTotalCopies < currentlyRented) {
                throw new IllegalStateException(
                    "総巻数は現在貸出中の巻数(" + currentlyRented + "巻)より少なくすることはできません。"
                );
            }
            
            book.setTotalCopies(newTotalCopies);
            // 대여 가능 권수 = 총 권수 - 대여 중인 권수
            book.setAvailableCopies((int)(newTotalCopies - currentlyRented));
        }
        
        // 파일 업로드 처리
        if (file != null && !file.isEmpty()) {
            // 기존 파일 삭제
            if (book.getFilePath() != null) {
                deleteFile(book.getFilePath());
            }
            
            String savedFileName = saveFile(file);
            book.setFileName(file.getOriginalFilename());
            book.setFilePath(UPLOAD_DIR + savedFileName);
        }
        
        return bookRepository.save(book);
    }
    
    // 도서 삭제
    public void deleteBook(Integer bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("書籍が見つかりません。"));
        
        // 파일 삭제
        if (book.getFilePath() != null) {
            deleteFile(book.getFilePath());
        }
        
        bookRepository.delete(book);
    }
    
    // 도서 목록 조회
    @Transactional(readOnly = true)
    public Page<Book> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }
    
    // 도서 검색
    @Transactional(readOnly = true)
    public Page<Book> searchBooks(String keyword, Pageable pageable) {
        return bookRepository.searchBooks(keyword, pageable);
    }
    
    // 장르별 조회
    @Transactional(readOnly = true)
    public Page<Book> getBooksByGenre(String genre, Pageable pageable) {
        return bookRepository.findByGenre(genre, pageable);
    }
    
    // 대여 가능한 도서만 조회
    @Transactional(readOnly = true)
    public Page<Book> getAvailableBooks(Pageable pageable) {
        return bookRepository.findAvailableBooks(pageable);
    }
    
    // 도서 상세 조회
    @Transactional(readOnly = true)
    public Book getBookById(Integer bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("書籍が見つかりません。"));
    }
    
    // 장르 목록 조회
    @Transactional(readOnly = true)
    public List<String> getAllGenres() {
        return List.of("アクション", "ロマンス", "ファンタジー", "スポーツ", "コメディ");
    }
    
    // 재고 증가 (반납 시)
    public void increaseAvailableCopies(Integer bookId) {
        Book book = getBookById(bookId);
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);
    }
    
    // 재고 감소 (대여 시)
    public void decreaseAvailableCopies(Integer bookId) {
        Book book = getBookById(bookId);
        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("在庫の貸出はありません。");
        }
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        book.setRentalCount((book.getRentalCount() == null ? 0 : book.getRentalCount()) + 1);
        bookRepository.save(book);
    }
    
    // 파일 저장
    private String saveFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String savedFileName = UUID.randomUUID().toString() + extension;
        
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        Path filePath = uploadPath.resolve(savedFileName);
        Files.copy(file.getInputStream(), filePath);
        
        return savedFileName;
    }
    
    // 파일 삭제
    private void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // 로그 처리
            e.printStackTrace();
        }
    }
}
