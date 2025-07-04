package com.expensetracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.expensetracker.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

	boolean existsByName(String name);
	public Category findByName(String name);

}
