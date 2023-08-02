package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.service.CategoryService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private static final String NOT_FOUND_MESSAGE = "Category with id=%s was not found";
    private final CategoryRepository categoryRepository;

    @Override
    public List<Category> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable).getContent();
    }

    @Override
    public Category getCategoryById(long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_MESSAGE, id)));
    }

    @Override
    public Category addCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(Category category) {
        if (!categoryRepository.existsById(category.getId())) {
            throw new NotFoundException(String.format(NOT_FOUND_MESSAGE, category.getId()));
        }
        return categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException(String.format(NOT_FOUND_MESSAGE, id));
        }
        categoryRepository.deleteById(id);
    }
}
