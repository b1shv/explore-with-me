package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.CategoryRequest;
import ru.practicum.model.Category;

@Component
public class CategoryMapper {
    public Category toCategory(CategoryRequest categoryDto) {
        return Category.builder()
                .name(categoryDto.getName())
                .build();
    }

    public Category toCategory(CategoryRequest categoryDto, long id) {
        return Category.builder()
                .id(id)
                .name(categoryDto.getName())
                .build();
    }

    public CategoryDto toDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
