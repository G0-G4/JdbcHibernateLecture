insert into authors (name)
values ('Robert Martin'),
       ('Martin Fowler'),
       ('Eric Evans');

insert into books (author_id, title, published_year)
values (1, 'Clean Code', 2008),
       (1, 'Clean Architecture', 2017),
       (2, 'Refactoring', 1999),
       (2, 'Patterns of Enterprise Application Architecture', 2002),
       (3, 'Domain-Driven Design', 2003);

insert into reviews (book_id, reviewer, rating, comment)
values (1, 'Alice', 5, 'Very practical.');

insert into reviews (book_id, reviewer, rating, comment)
values (1, 'Bob', 4, 'Still relevant.'),
       (2, 'Clara', 4, 'Solid principles.'),
       (3, 'Dan', 5, 'Must read for legacy code.'),
       (3, 'Eva', 5, 'Excellent examples.'),
       (4, 'Frank', 4, 'Dense but useful.'),
       (5, 'Grace', 5, 'Deep and foundational.');
