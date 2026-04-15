drop table if exists reviews;
drop table if exists books;
drop table if exists authors;

create table authors (
  id bigint generated always as identity primary key,
  name varchar(120) not null
);

create table books (
  id bigint generated always as identity primary key,
  author_id bigint not null references authors(id),
  title varchar(200) not null,
  published_year integer not null
);

create table reviews (
  id bigint generated always as identity primary key,
  book_id bigint not null references books(id),
  reviewer varchar(120) not null,
  rating integer not null check (rating between 1 and 5),
  comment varchar(500) not null
);
