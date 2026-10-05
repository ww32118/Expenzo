package com.Expenzo_Backend.Expenzo.repository;

import com.Expenzo_Backend.Expenzo.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Category findCategoriesById(Long id);

    List<Category> findByUserId(Long userId);

}
