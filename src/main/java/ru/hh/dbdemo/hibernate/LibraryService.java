package ru.hh.dbdemo.hibernate;

import ru.hh.dbdemo.dto.AuthorSummaryDto;
import ru.hh.dbdemo.dto.CreateBookRequest;
import ru.hh.dbdemo.dto.PageDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
    for (Author author : authorDao.findAll()) {
      result.add(new AuthorSummaryDto(author.getId(), author.getName(), author.getBooks().size()));
    }
    return result;
  }

  @Transactional(readOnly = true)
  public PageDto<AuthorSummaryDto> findAuthorsPageWithBookCounts(int page, int size) {
    PageDto<Author> authorsPage = authorDao.findPageWithBooksNaive(page, size);
    List<AuthorSummaryDto> items = authorsPage.items().stream()
        .map(author -> new AuthorSummaryDto(author.getId(), author.getName(), author.getBooks().size()))
        .toList();
    return new PageDto<>(items, authorsPage.page(), authorsPage.size(), authorsPage.total());
  }

  public Author findAuthorEntity(long authorId) {
    return authorDao.findById(authorId)
        .orElseThrow(() -> new EntityNotFoundException("Author %s not found".formatted(authorId)));
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
