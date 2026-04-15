package ru.hh.dbdemo.dto;

import java.util.List;

public record AuthorDetailsDto(Long id, String name, List<BookDetailsDto> books) {
}
