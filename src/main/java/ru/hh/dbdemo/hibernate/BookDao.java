package ru.hh.dbdemo.hibernate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class BookDao {

  @PersistenceContext
  private EntityManager entityManager;

  public Book save(Book book) {
    entityManager.persist(book);
    entityManager.flush();
    return book;
  }
}
