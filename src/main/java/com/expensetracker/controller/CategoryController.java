package com.expensetracker.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expensetracker.model.Category;
import com.expensetracker.service.CategoryService;

@RestController
public class CategoryController {
	@Autowired
	private CategoryService categoryService;

	@GetMapping("/categories")
	public ResponseEntity<List<Category>> getAllCategories(){
		
		return new ResponseEntity<List<Category>>(categoryService.getAll(),HttpStatus.OK);
	}
	
	
	
}
