package ru.hh.dbdemo.hibernate;

import ru.hh.dbdemo.dto.AuthorDetailsDto;
import ru.hh.dbdemo.dto.AuthorSummaryDto;
import ru.hh.dbdemo.dto.BookDetailsDto;
import ru.hh.dbdemo.dto.CreateBookRequest;
import ru.hh.dbdemo.dto.PageDto;
import ru.hh.dbdemo.dto.ReviewDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LibraryService {

  private final AuthorDao authorDao;
  private final BookDao bookDao;

  public LibraryService(
      AuthorDao authorDao,
      BookDao bookDao
  ) {
    this.authorDao = authorDao;
    this.bookDao = bookDao;
  }

  @Transactional(readOnly = true)
  public List<AuthorSummaryDto> findAuthorsWithBookCounts() {
    List<AuthorSummaryDto> result = new ArrayList<>();
    for (Author author : authorDao.findAllWithBooks()) {
      result.add(new AuthorSummaryDto(author.getId(), author.getName(), author.getBooks().size()));
    }
    return result;
  }

  @Transactional(readOnly = true)
  public PageDto<AuthorSummaryDto> findAuthorsPageWithBookCounts(int page, int size) {
    PageDto<Long> authorIds = authorDao.findAuthorIds(page, size);
    if (authorIds.items().isEmpty()) {
      return new PageDto<>(List.of(), page, size, authorIds.total());
    }

    List<Author> authors = authorDao.findAllByIdsWithBooks(authorIds.items());
    List<AuthorSummaryDto> items = new ArrayList<>(authors.size());
    for (Author author : authors) {
      items.add(new AuthorSummaryDto(author.getId(), author.getName(), author.getBooks().size()));
    }
    return new PageDto<>(items, page, size, authorIds.total());
  }

  @Transactional(readOnly = true)
  public AuthorDetailsDto findAuthorDetails(long authorId) {
    Author author = authorDao.findByIdWithBooks(authorId)
        .orElseThrow(() -> new EntityNotFoundException("Author %s not found".formatted(authorId)));

    Map<Long, List<ReviewDto>> reviewsByBookId = new HashMap<>();
    for (Review review : authorDao.findReviewsByAuthorId(authorId)) {
      Long bookId = review.getBook().getId();
      reviewsByBookId.computeIfAbsent(bookId, ignored -> new ArrayList<>())
          .add(new ReviewDto(
              review.getId(),
              review.getReviewer(),
              review.getRating(),
              review.getComment()
          ));
    }

    List<BookDetailsDto> books = author.getBooks().stream()
        .map(book -> new BookDetailsDto(
            book.getId(),
            book.getTitle(),
            book.getPublishedYear(),
            reviewsByBookId.getOrDefault(book.getId(), List.of())
        ))
        .toList();
    return new AuthorDetailsDto(author.getId(), author.getName(), books);
  }

  @Transactional
  public Long createBook(CreateBookRequest request) {
    Author author = authorDao.findById(request.authorId())
        .orElseThrow(() -> new EntityNotFoundException("Author %s not found".formatted(request.authorId())));

    Book book = new Book();
    book.setAuthor(author);
    book.setTitle(request.title());
    book.setPublishedYear(request.publishedYear());
    return bookDao.save(book).getId();
  }
}
