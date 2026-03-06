package com.ectrvia.trivia.repository;

import com.ectrvia.trivia.entity.CategoryData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryData, Long> {
    List<CategoryData> findByIsActiveTrue();
    boolean existsByNameIgnoreCase(String name);
}
