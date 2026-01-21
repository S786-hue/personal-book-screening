package com.example.demo;

import com.example.demo.db.Book;
import com.example.demo.db.BookRepository;
import com.example.demo.google.GoogleBook;
import com.example.demo.google.GoogleBookService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


@RestController
public class BookController {
    private final BookRepository bookRepository;
    private final GoogleBookService googleBookService;

    @Autowired
    public BookController(BookRepository bookRepository, GoogleBookService googleBookService) {
        this.bookRepository = bookRepository;
        this.googleBookService = googleBookService;
    }

    @GetMapping("/books")
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @GetMapping("/google")
    public GoogleBook searchGoogleBooks(@RequestParam("q") String query,
                                        @RequestParam(value = "maxResults", required = false) Integer maxResults,
                                        @RequestParam(value = "startIndex", required = false) Integer startIndex) {
        return googleBookService.searchBooks(query, maxResults, startIndex);
    }
    @PostMapping("/books/{googleId}")
    public ResponseEntity<Book> addBookFromGoogle(@PathVariable String googleId) {
        try {
            GoogleBook.Item googleItem = googleBookService.getBookById(googleId);

            if (googleItem == null || googleItem.volumeInfo() == null) {
                return ResponseEntity.badRequest().build();
            }

            GoogleBook.VolumeInfo info = googleItem.volumeInfo();

            String author = null;
            if (info.authors() != null && !info.authors().isEmpty()) {
                author = info.authors().get(0);
            }

            Book book = new Book(
                    googleItem.id(),
                    info.title(),
                    author,
                    info.pageCount()
            );

            Book savedBook = bookRepository.save(book);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedBook);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
