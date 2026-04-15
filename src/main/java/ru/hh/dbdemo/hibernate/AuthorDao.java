package ru.hh.dbdemo.hibernate;

import ru.hh.dbdemo.dto.PageDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AuthorDao {

  @PersistenceContext
  private EntityManager entityManager;

  public List<Author> findAllWithBooks() {
    return entityManager.createQuery(
            """
                select distinct a
                from Author a
                left join fetch a.books
                order by a.id
                """,
            Author.class
        )
        .getResultList();
  }

  public PageDto<Long> findAuthorIds(int page, int size) {
    List<Long> ids = entityManager.createQuery(
            "select a.id from Author a order by a.id",
            Long.class
        )
        .setFirstResult(page * size)
        .setMaxResults(size)
        .getResultList();

    Long total = entityManager.createQuery(
            "select count(a) from Author a",
            Long.class
        )
        .getSingleResult();

    return new PageDto<>(ids, page, size, total);
  }

  public List<Author> findAllByIdsWithBooks(List<Long> authorIds) {
    return entityManager.createQuery(
            """
                select distinct a
                from Author a
                left join fetch a.books
                where a.id in :authorIds
                order by a.id
                """,
            Author.class
        )
        .setParameter("authorIds", authorIds)
        .getResultList();
  }

  public Optional<Author> findByIdWithBooks(long authorId) {
    List<Author> result = entityManager.createQuery(
            """
                select distinct a
                from Author a
                left join fetch a.books
                where a.id = :authorId
                """,
            Author.class
        )
        .setParameter("authorId", authorId)
        .getResultList();
    return result.stream().findFirst();
  }

  public List<Review> findReviewsByAuthorId(long authorId) {
    return entityManager.createQuery(
            """
                select r
                from Review r
                join fetch r.book b
                where b.author.id = :authorId
                order by b.id, r.id
                """,
            Review.class
        )
        .setParameter("authorId", authorId)
        .getResultList();
  }

  public Optional<Author> findById(long authorId) {
    return Optional.ofNullable(entityManager.find(Author.class, authorId));
  }
}
