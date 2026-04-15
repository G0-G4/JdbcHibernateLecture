package ru.hh.dbdemo.dto;

import java.util.List;

public record BookDetailsDto(Long id, String title, int publishedYear, List<ReviewDto> reviews) {
}
