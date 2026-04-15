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

  public List<Author> findAll() {
    return entityManager.createQuery(
            "select a from Author a order by a.id",
            Author.class
        )
        .getResultList();
  }

  public PageDto<Author> findPageWithBooksNaive(int page, int size) {
    List<Author> content = entityManager.createQuery(
            """
                select distinct a
                from Author a
                left join fetch a.books b
                order by a.id
                """,
            Author.class
        )
        .setFirstResult(page * size)
        .setMaxResults(size)
        .getResultList();

    Long total = entityManager.createQuery(
            "select count(a) from Author a",
            Long.class
        )
        .getSingleResult();

    return new PageDto<>(content, page, size, total);
  }

  public Optional<Author> findById(long authorId) {
    return Optional.ofNullable(entityManager.find(Author.class, authorId));
  }
}
