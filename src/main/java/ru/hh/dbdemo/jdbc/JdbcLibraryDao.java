package ru.hh.dbdemo.jdbc;

import ru.hh.dbdemo.dto.AuthorDetailsDto;
import ru.hh.dbdemo.dto.AuthorSummaryDto;
import ru.hh.dbdemo.dto.BookDetailsDto;
import ru.hh.dbdemo.dto.CreateBookRequest;
import ru.hh.dbdemo.dto.ReviewDto;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JdbcLibraryDao {

  private final DataSource dataSource;

  public JdbcLibraryDao(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public List<AuthorSummaryDto> findAllAuthors() {
    String sql = """
        select a.id, a.name, count(b.id) as books_count
        from authors a
        left join books b on b.author_id = a.id
        group by a.id, a.name
        order by a.id
        """;

    List<AuthorSummaryDto> authors = new ArrayList<>();
    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
         ResultSet resultSet = statement.executeQuery()) {
      while (resultSet.next()) {
        authors.add(new AuthorSummaryDto(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getInt("books_count")
        ));
      }
    } catch (SQLException e) {
      throw new IllegalStateException("Cannot load authors", e);
    }
    return authors;
  }

  public List<AuthorSummaryDto> findAuthorsPage(int page, int size) {
    String sql = """
        select a.id, a.name, count(b.id) as books_count
        from authors a
        left join books b on b.author_id = a.id
        group by a.id, a.name
        order by a.id
        limit ? offset ?
        """;

    List<AuthorSummaryDto> authors = new ArrayList<>();
    int offset = page * size;
    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setInt(1, size);
      statement.setInt(2, offset);
      try (ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          authors.add(new AuthorSummaryDto(
              resultSet.getLong("id"),
              resultSet.getString("name"),
              resultSet.getInt("books_count")
          ));
        }
      }
    } catch (SQLException e) {
      throw new IllegalStateException("Cannot load authors page", e);
    }
    return authors;
  }

  public long countAuthors() {
    String sql = "select count(*) from authors";
    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
         ResultSet resultSet = statement.executeQuery()) {
      resultSet.next();
      return resultSet.getLong(1);
    } catch (SQLException e) {
      throw new IllegalStateException("Cannot count authors", e);
    }
  }

  public AuthorDetailsDto findAuthorDetails(long authorId) {
    String authorSql = "select id, name from authors where id = ?";
    String booksSql = "select id, title, published_year from books where author_id = ? order by id";
    String reviewsSql = "select id, reviewer, rating, comment from reviews where book_id = ? order by id";

    try (Connection connection = dataSource.getConnection()) {
      String authorName;
      try (PreparedStatement authorStatement = connection.prepareStatement(authorSql)) {
        authorStatement.setLong(1, authorId);
        try (ResultSet authorResult = authorStatement.executeQuery()) {
          if (!authorResult.next()) {
            return null;
          }
          authorName = authorResult.getString("name");
        }
      }

      List<BookDetailsDto> books = new ArrayList<>();
      try (PreparedStatement booksStatement = connection.prepareStatement(booksSql)) {
        booksStatement.setLong(1, authorId);
        try (ResultSet booksResult = booksStatement.executeQuery()) {
          while (booksResult.next()) {
            long bookId = booksResult.getLong("id");
            List<ReviewDto> reviews = new ArrayList<>();

            try (PreparedStatement reviewStatement = connection.prepareStatement(reviewsSql)) {
              reviewStatement.setLong(1, bookId);
              try (ResultSet reviewsResult = reviewStatement.executeQuery()) {
                while (reviewsResult.next()) {
                  reviews.add(new ReviewDto(
                      reviewsResult.getLong("id"),
                      reviewsResult.getString("reviewer"),
                      reviewsResult.getInt("rating"),
                      reviewsResult.getString("comment")
                  ));
                }
              }
            }

            books.add(new BookDetailsDto(
                bookId,
                booksResult.getString("title"),
                booksResult.getInt("published_year"),
                reviews
            ));
          }
        }
      }

      return new AuthorDetailsDto(authorId, authorName, books);
    } catch (SQLException e) {
      throw new IllegalStateException("Cannot load author details", e);
    }
  }

  public long createBook(CreateBookRequest request) {
    String sql = "insert into books(author_id, title, published_year) values (?, ?, ?)";

    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setLong(1, request.authorId());
      statement.setString(2, request.title());
      statement.setInt(3, request.publishedYear());

      int updatedRows = statement.executeUpdate();
      if (updatedRows == 0) {
        throw new IllegalStateException("Book insert did not affect any rows");
      }

      try (ResultSet keys = statement.getGeneratedKeys()) {
        if (!keys.next()) {
          throw new IllegalStateException("Book insert did not return generated id");
        }
        return keys.getLong(1);
      }
    } catch (SQLException e) {
      throw new IllegalStateException("Cannot create book", e);
    }
  }

}
