package ru.hh.dbdemo.dto;

public record CreateBookRequest(Long authorId, String title, int publishedYear) {
}
