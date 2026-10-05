package com.Expenzo_Backend.Expenzo.service;

import com.Expenzo_Backend.Expenzo.dto.CategoryRequest;
import com.Expenzo_Backend.Expenzo.dto.CategoryResponse;
import com.Expenzo_Backend.Expenzo.exception.NoCategoryExistsException;
import com.Expenzo_Backend.Expenzo.exception.UserNotFoundException;
import com.Expenzo_Backend.Expenzo.model.Category;
import com.Expenzo_Backend.Expenzo.model.User;
import com.Expenzo_Backend.Expenzo.repository.CategoryRepository;
import com.Expenzo_Backend.Expenzo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    // ───────────────────────────── ADD ─────────────────────────────
    public CategoryResponse addCategory(CategoryRequest categoryRequest, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User Not Found By Email : " + email));

        if (categoryRequest.getName() == null || categoryRequest.getName().trim().isEmpty()) {
            throw new RuntimeException("Category name is required");
        }

        Category category = new Category();
        category.setName(categoryRequest.getName().trim());
        category.setUser(user);

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    // ───────────────────────────── UPDATE ─────────────────────────────
    public CategoryResponse updateCategory(Long id, CategoryRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NoCategoryExistsException("Category not found"));

        if (!category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized");
        }

        category.setName(request.getName());
        return mapToResponse(categoryRepository.save(category));
    }

    // ───────────────────────────── READ ─────────────────────────────
    public CategoryResponse getCategoryById(Long id) {
        // ✅ Removed unused `user` variable
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NoCategoryExistsException("Category Doesn't Exist By id : " + id));

        return mapToResponse(category);
    }

    public List<CategoryResponse> getAllCategories(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return categoryRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ───────────────────────────── DELETE ─────────────────────────────
    @Transactional
    public void deleteCategory(Long categoryId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NoCategoryExistsException("Category not found"));

        if (!category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You don't have permission to delete this category");
        }

        categoryRepository.delete(category);
    }

    // ───────────────────────────── MAPPER ─────────────────────────────
    private CategoryResponse mapToResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());

        if (category.getUser() != null) {
            response.setUserId(category.getUser().getId());
        }

        return response;
    }
}