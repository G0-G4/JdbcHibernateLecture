package ru.hh.dbdemo.hibernate;

import ru.hh.dbdemo.dto.AuthorDetailsDto;
import ru.hh.dbdemo.dto.AuthorSummaryDto;
import ru.hh.dbdemo.dto.BookDetailsDto;
import ru.hh.dbdemo.dto.CreateBookRequest;
import ru.hh.dbdemo.dto.PageDto;
import ru.hh.dbdemo.dto.ReviewDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.LazyInitializationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/library")
@Tag(name = "Hibernate", description = "Endpoints with common Hibernate pitfalls")
public class LibraryController {

  private final LibraryService service;

  public LibraryController(LibraryService service) {
    this.service = service;
  }

  @GetMapping("/authors")
  @Operation(summary = "List authors", description = "Contains N+1 query problem by design")
  public List<AuthorSummaryDto> getAuthors() {
    return service.findAuthorsWithBookCounts();
  }

  @GetMapping("/authors/paged")
  @Operation(summary = "List authors page", description = "Naive JOIN FETCH + Pageable pitfall")
  public PageDto<AuthorSummaryDto> getAuthorsPaged(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    return service.findAuthorsPageWithBookCounts(page, size);
  }

  @GetMapping("/authors/{authorId}")
  @Operation(summary = "Get author details", description = "Triggers LazyInitializationException by design")
  public AuthorDetailsDto getAuthor(@PathVariable long authorId) {
    Author author = service.findAuthorEntity(authorId);
    List<BookDetailsDto> books = author.getBooks().stream()
        .map(book -> new BookDetailsDto(
            book.getId(),
            book.getTitle(),
            book.getPublishedYear(),
            book.getReviews().stream()
                .map(review -> new ReviewDto(
                    review.getId(),
                    review.getReviewer(),
                    review.getRating(),
                    review.getComment()
                ))
                .toList()
        ))
        .toList();
    return new AuthorDetailsDto(author.getId(), author.getName(), books);
  }

  @PostMapping("/books")
  @Operation(summary = "Create book", description = "Simple create endpoint with Hibernate DAO")
  public ResponseEntity<Void> createBook(@RequestBody CreateBookRequest payload) {
    Long bookId = service.createBook(payload);
    return ResponseEntity.status(HttpStatus.CREATED)
        .location(URI.create("/api/library/books/" + bookId))
        .build();
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<String> handleNotFound(EntityNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
  }

  @ExceptionHandler(LazyInitializationException.class)
  public ResponseEntity<String> handleLazyInit(LazyInitializationException ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("LazyInitializationException demo triggered: " + ex.getMessage());
  }
}
