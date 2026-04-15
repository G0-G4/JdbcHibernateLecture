package ru.hh.dbdemo.jdbc;

import ru.hh.dbdemo.dto.AuthorDetailsDto;
import ru.hh.dbdemo.dto.AuthorSummaryDto;
import ru.hh.dbdemo.dto.CreateBookRequest;
import ru.hh.dbdemo.dto.PageDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JdbcLibraryService {

  private final JdbcLibraryDao dao;

  public JdbcLibraryService(JdbcLibraryDao dao) {
    this.dao = dao;
  }

  public List<AuthorSummaryDto> findAllAuthors() {
    return dao.findAllAuthors();
  }

  public PageDto<AuthorSummaryDto> findAuthorsPage(int page, int size) {
    List<AuthorSummaryDto> items = dao.findAuthorsPage(page, size);
    long total = dao.countAuthors();
    return new PageDto<>(items, page, size, total);
  }

  public AuthorDetailsDto findAuthorById(long authorId) {
    return dao.findAuthorDetails(authorId);
  }

  public long createBook(CreateBookRequest request) {
    return dao.createBook(request);
  }
}
