package ru.practicum.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.model.Category;

import java.util.List;

public interface CategoryService {

    List<Category> getAllCategories(Pageable pageable);

    Category getCategoryById(long id);

    Category addCategory(Category category);

    Category updateCategory(Category category);

    void deleteCategory(long id);
}
