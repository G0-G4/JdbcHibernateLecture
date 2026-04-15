package ru.hh.dbdemo.dto;

public record ReviewDto(Long id, String reviewer, int rating, String comment) {
}
