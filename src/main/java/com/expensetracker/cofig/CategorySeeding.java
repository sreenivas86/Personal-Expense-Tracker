package com.expensetracker.cofig;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.expensetracker.model.Category;
import com.expensetracker.repository.CategoryRepository;

import jakarta.annotation.PostConstruct;

@Component
public class CategorySeeding {
	 @Autowired
	    private CategoryRepository categoryRepository;

	    @PostConstruct
	    public void seedCategories() {
	    	Map<String, String> defaultCategories = Map.ofEntries(
	    		    Map.entry("Rent / Mortgage", "#FFB6C1"),
	    		    Map.entry("Food / Groceries", "#FFA07A"),
	    		    Map.entry("Transportation", "#20B2AA"),
	    		    Map.entry("Utilities", "#87CEFA"),
	    		    Map.entry("Healthcare", "#98FB98"),
	    		    Map.entry("Education", "#FFD700"),
	    		    Map.entry("Work Expenses", "#C0C0C0"),
	    		    Map.entry("Entertainment", "#FF69B4"),
	    		    Map.entry("Clothing", "#DA70D6"),
	    		    Map.entry("Shopping", "#8A2BE2"),
	    		    Map.entry("Debt Repayment", "#DC143C"),
	    		    Map.entry("Travel", "#00CED1"),
	    		    Map.entry("Personal Care", "#F08080"),
	    		    Map.entry("Savings & Investments", "#3CB371"),
	    		    Map.entry("Donations & Charity", "#FF4500"),
	    		    Map.entry("Home Maintenance", "#D2691E"),
	    		    Map.entry("Pet Expenses", "#BA55D3"),
	    		    Map.entry("Subscriptions", "#00BFFF"),
	    		    Map.entry("Others", "#808080")
	    		);


	        for (Map.Entry<String, String> entry : defaultCategories.entrySet()) {
	            if (!categoryRepository.existsByName(entry.getKey())) {
	                Category category = new Category();
	                category.setName(entry.getKey());
	                category.setColorCode(entry.getValue());
	                categoryRepository.save(category);
	            }
	        }
	    }
	}
	