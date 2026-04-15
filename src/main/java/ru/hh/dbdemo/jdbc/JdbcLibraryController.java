package ru.hh.dbdemo.jdbc;

import ru.hh.dbdemo.dto.AuthorDetailsDto;
import ru.hh.dbdemo.dto.AuthorSummaryDto;
import ru.hh.dbdemo.dto.CreateBookRequest;
import ru.hh.dbdemo.dto.PageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@Tag(name = "JDBC", description = "Vanilla JDBC endpoints")
public class JdbcLibraryController {

  private final JdbcLibraryService service;

  public JdbcLibraryController(JdbcLibraryService service) {
    this.service = service;
  }

  @GetMapping("/authors")
  @Operation(summary = "List all authors", description = "Returns authors and amount of books")
  public List<AuthorSummaryDto> getAuthors() {
    return service.findAllAuthors();
  }

  @GetMapping("/authors/paged")
  @Operation(summary = "List authors page", description = "Returns one page of authors")
  public PageDto<AuthorSummaryDto> getAuthorsPaged(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    return service.findAuthorsPage(page, size);
  }

  @GetMapping("/authors/{authorId}")
  @Operation(summary = "Get author details", description = "Returns author with books and reviews")
  public ResponseEntity<AuthorDetailsDto> getAuthor(@PathVariable long authorId) {
    AuthorDetailsDto author = service.findAuthorById(authorId);
    if (author == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(author);
  }

  @PostMapping("/books")
  @Operation(summary = "Create book", description = "Creates a new book for an author")
  public ResponseEntity<Void> createBook(@RequestBody CreateBookRequest request) {
    long createdBookId = service.createBook(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .location(URI.create("/api/library/books/" + createdBookId))
        .build();
  }

}
