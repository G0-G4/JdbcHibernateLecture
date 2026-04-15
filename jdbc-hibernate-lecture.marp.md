---
marp: true
html: true
theme: gaia
paginate: true
size: 16:9
title: JDBC и Hibernate на одном приложении
description: Практическая лекция по JDBC и vanilla Hibernate (HQL) на проекте dbdemo
---

![bg](title1.png)


---
## Обо мне

- Java backend разработчик в hh
- До этого разрабатывал ETL фреймворк для банков
- Магистр, но не йода


---

## О чем лекция

- Как выглядит доступ к БД через чистый JDBC
- Как тот же API выглядит на Hibernate/JPA
- Какие типовые проблемы возникают в ORM

---

## Проект и домен

- Репозиторий: https://github.com/G0-G4/JdbcHibernateLecture
- СУБД: PostgreSQL в Docker (`docker-compose.yml`)
- Домен: `authors -> books -> reviews`

<center>

![h:300](/Users/g.grishenkov/projects/dbdemo/qrcode_github.com.png)

</center>

---

## Блок 1. JDBC


- JDBC — низкоуровневый API Java для работы с реляционными БД
- Весь доступ к данным строится вокруг `Connection`, `PreparedStatement`, `ResultSet`
- SQL пишем вручную, явно контролируем `JOIN`, `LIMIT/OFFSET`, индексы
- Транзакции: либо autocommit, либо ручной `commit/rollback`

---

### Почему с него начинаем

- Лежит под капотом Hibernate
- Видно каждый шаг взаимодействия с БД
- Нет «магии», легче понять цену каждого запроса
- Хорошо показывает, что потом автоматизирует ORM

---

## JDBC: жизненный цикл запроса

- `DataSource` -> `Connection`
- `PreparedStatement`
- `executeQuery / executeUpdate`
- `ResultSet` + ручной маппинг
- Закрытие ресурсов (`try-with-resources`)

---

![bg 90%](jdbc.png)

---

## JDBC в проекте

- Контроллер: `src/main/java/ru/hh/dbdemo/jdbc/JdbcLibraryController.java`
- Сервис: `src/main/java/ru/hh/dbdemo/jdbc/JdbcLibraryService.java`
- DAO: `src/main/java/ru/hh/dbdemo/jdbc/JdbcLibraryDao.java`

Основная «боль» находится в DAO: SQL, маппинг, обработка ошибок, ключи.

---

## JDBC пример: ручной маппинг

```java
while(resultSet.next()){
    authors
    .add(new AuthorSummaryDto(
      resultSet.getLong("id"),
      resultSet.getString("name"),
      resultSet.getInt("books_count")));
}
```

---

## JDBC пример: create + generated key

```java
PreparedStatement st = connection.prepareStatement(
    sql, Statement.RETURN_GENERATED_KEYS);
st.executeUpdate();

ResultSet keys = st.getGeneratedKeys();
```

Каждую деталь контролируем вручную.

---

## JDBC демо (live)

1. `git checkout b56759f`
2. `GET /api/library/authors`
3. `GET /api/library/authors/1`
4. `POST /api/library/books`


---

## JDBC: что не так на росте проекта

- Много boilerplate для каждого нового запроса
- Повторный маппинг сущностей и связей
- Ручная работа с транзакциями/исключениями
- Сложнее масштабировать богатую доменную модель

---

## Блок 2. Hibernate (проблемная версия)


- Hibernate — ORM поверх JDBC: работаем с объектами, а не с `ResultSet`
- `EntityManager` держит persistence context и отслеживает изменения (`dirty checking`)
- HQL описывает запросы к сущностям и их связям
- Ключ к пониманию ошибок — жизненный цикл entity

---

![bg](lifecycle.jpeg)

---

### Цель этапа

- Сократить boilerplate
- Посмотреть типичные примеры, где ORM «стреляет в ногу»

---

## Переключение на Hibernate

- `git checkout b64013d`
- Пакет: `src/main/java/ru/hh/dbdemo/hibernate/*`
- `@Entity`: `Author`, `Book`, `Review`
- DAO + HQL через `EntityManager`

Внешний API и DTO остаются прежними.

---

## Что стало лучше сразу

- Меньше ручного SQL
- Меньше кода на CRUD
- Связи описаны в модели (`@OneToMany`, `@ManyToOne`)
- Быстрее разрабатывать фичи

---

## Проблема 1: N+1

Сценарий:

- Эндпоинт `GET /api/library/authors`
- Для каждого автора читаются `books` через lazy-коллекцию
- Получаем каскад дополнительных запросов

Симптом: много SQL в логах при одном HTTP-запросе.

---

## Проблема 2: LazyInitializationException

Сценарий:

- Эндпоинт `GET /api/library/authors/{id}`
- Сервис возвращает entity, а сериализация идет вне транзакции
- Доступ к lazy-связям ломается

Симптом: `LazyInitializationException` в ответе/логах.

---

## Проблема 3: `JOIN FETCH` + пагинация

Сценарий `GET /api/library/authors/paged?page=0&size=2`:

- Разработчик добавил `JOIN FETCH` для коллекций и сразу SQL-level пагинацию (`setFirstResult` / `setMaxResults`)
- Hibernate не может корректно пагинировать fetch-join по коллекции на SQL уровне
- Включается in-memory pagination и растут время/память

Симптом: предупреждение `HHH90003004` и деградация на больших данных.

---

## Hibernate-problem демо (live)

1. `GET /api/library/authors` -> обсуждаем `N+1`
2. `GET /api/library/authors/1` -> ловим lazy-проблему
3. `GET /api/library/authors/paged?page=0&size=2` -> видим проблему fetch-join + pagination

---

## Почему это важно

- Проблемы не видны в happy-path
- На тестовых данных может быть «все быстро»
- В проде перерастают в деградацию и инциденты

ORM не избавляет от понимания SQL и ограничений ORM-провайдера.

---

## Блок 3. Hibernate fix-версия


- Проектируем fetch-план под конкретный endpoint, а не «на все случаи»
- Маппим entity -> DTO внутри транзакции, наружу отдаем только DTO
- Для пагинации графа используем двухшаговый подход: `ids -> fetch`
- Не делаем `JOIN FETCH` сразу для нескольких `List`-коллекций
- Проверяем SQL-логи так же внимательно, как HTTP-ответы
- ORM ускоряет разработку, но не отменяет понимание SQL


---

## Переключение на fix-коммит

- `git checkout main`
- API и DTO без изменений
- Меняется только внутренняя реализация

---

## Фикс 1: `JOIN FETCH` против N+1

`AuthorDao`:

```java

entityManager.createQuery("""
    select distinct a
    from Author a
    left join fetch a.books
    order by a.id
    """, Author.class).getResultList();
```

Один запрос вместо серии запросов на автора.

---

## Фикс 2: DTO-маппинг в транзакции

- В сервисе читаем граф данных внутри `@Transactional(readOnly = true)`
- Маппим в `AuthorDetailsDto`
- Контроллер наружу entity не отдает

Результат: нет `LazyInitializationException` на выдаче.

---

## Фикс 3: двухшаговая пагинация с `JOIN FETCH`

- Шаг 1: отдельный запрос ID текущей страницы (`ids + total` в `PageDto<Long>`)
- Шаг 2: `JOIN FETCH` `authors + books` по списку ID из текущей страницы

Результат: корректная SQL-пагинация и предсказуемая загрузка.

---

## Hibernate-fix демо (live)

1. `GET /api/library/authors` -> меньше SQL, нет N+1 каскада
2. `GET /api/library/authors/1` -> стабильно отдает DTO
3. `GET /api/library/authors/paged?page=0&size=2` -> корректная страница без in-memory pagination

---

## Практический чеклист для Hibernate

- Для чтения графа используйте `JOIN FETCH` и явные HQL-запросы
- Не смешивайте `JOIN FETCH` и пагинацию в один запрос
- Для пагинации графа используй двухшаговый подход (IDs -> fetch)
- Не отдавай entity напрямую в API


---

## Полезные ссылки
- Spring Data JDBC (середина между jdbc и Hibernate) https://spring.io/projects/spring-data-jdbc
- Vlad Mihalcea (практика Hibernate): https://vladmihalcea.com
- https://proselyte.net/tutorials/hibernate-tutorial/
- https://proselyte.net/jpa-n-plus-1-select-problem/
