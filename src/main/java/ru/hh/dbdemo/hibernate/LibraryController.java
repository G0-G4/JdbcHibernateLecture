package ru.hh.dbdemo.hibernate;

import ru.hh.dbdemo.dto.AuthorDetailsDto;
import ru.hh.dbdemo.dto.AuthorSummaryDto;
import ru.hh.dbdemo.dto.CreateBookRequest;
import ru.hh.dbdemo.dto.PageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
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
@Tag(name = "Hibernate", description = "Hibernate endpoints with JOIN FETCH improvements")
public class LibraryController {

  private final LibraryService service;

  public LibraryController(LibraryService service) {
    this.service = service;
  }

  @GetMapping("/authors")
  @Operation(summary = "List authors", description = "Uses JOIN FETCH to avoid N+1 queries")
  public List<AuthorSummaryDto> getAuthors() {
    return service.findAuthorsWithBookCounts();
  }

  @GetMapping("/authors/paged")
  @Operation(summary = "List authors page", description = "Uses two-step pagination with JOIN FETCH")
  public PageDto<AuthorSummaryDto> getAuthorsPaged(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    return service.findAuthorsPageWithBookCounts(page, size);
  }

  @GetMapping("/authors/{authorId}")
  @Operation(summary = "Get author details", description = "Loads graph in transaction with JOIN FETCH")
  public AuthorDetailsDto getAuthor(@PathVariable long authorId) {
    return service.findAuthorDetails(authorId);
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
}
