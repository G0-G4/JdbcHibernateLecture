# JDBC vs Hibernate Demo

Demo project for a lecture that evolves through commits:

1. Vanilla JDBC with noticeable boilerplate.
2. Vanilla Hibernate (HQL + DAO) version with common ORM pitfalls.
3. Hibernate fixes for lazy loading, N+1, and JOIN FETCH pagination.

## Run PostgreSQL

```bash
docker compose up -d
```

## Run application

```bash
./mvnw spring-boot:run
```

## OpenAPI

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Raw spec: `http://localhost:8080/v3/api-docs`

## Common API (all commits)

- `GET /api/library/authors`
- `GET /api/library/authors/paged?page=0&size=20`
- `GET /api/library/authors/{id}`
- `POST /api/library/books`

## Commit 2 notes

- `GET /api/library/authors` has N+1 query issue.
- `GET /api/library/authors/paged` uses naive JOIN FETCH with Pageable (in-memory pagination pitfall).
- `GET /api/library/authors/{id}` can trigger `LazyInitializationException`.
- Paged responses use a simplified custom envelope: `PageDto<T>`.

Hibernate stage uses explicit DAOs and HQL instead of Spring Data repositories.
