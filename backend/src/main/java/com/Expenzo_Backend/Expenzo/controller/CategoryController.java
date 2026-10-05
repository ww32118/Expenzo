package com.Expenzo_Backend.Expenzo.controller;

import com.Expenzo_Backend.Expenzo.dto.CategoryRequest;
import com.Expenzo_Backend.Expenzo.dto.CategoryResponse;
//import com.Expenzo_Backend.Expenzo.dto.UpdateCategoryRequest;
import com.Expenzo_Backend.Expenzo.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/add-category")
    public ResponseEntity<CategoryResponse> addCategory(
            @RequestBody CategoryRequest request,
            Principal principal) {  // 🔑 Get logged-in user

        CategoryResponse response = categoryService.addCategory(request, principal.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all-categories")
    public List<CategoryResponse> getAllCategories(Principal principal) {
        return categoryService.getAllCategories(principal.getName());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id, Principal principal) {
        categoryService.deleteCategory(id, principal.getName());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryRequest request,
            Principal principal) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request, principal.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {

        CategoryResponse response = categoryService.getCategoryById(id);

        return ResponseEntity.ok(response);
    }
}
