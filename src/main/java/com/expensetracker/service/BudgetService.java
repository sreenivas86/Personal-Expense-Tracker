package com.expensetracker.service;

import java.util.List;
import java.util.Optional;

import com.expensetracker.bean.ServiceResponse;
import com.expensetracker.model.Budget;

public interface BudgetService {

	public ServiceResponse<String> addBudjet(Budget budget);
	public ServiceResponse<String> updateBudget(Long id,Budget budget);
	public ServiceResponse<String> deleteBudget(Long id);
	public ServiceResponse<Optional<Budget>> findBudget(Long id);
	public ServiceResponse<List<Budget>> findBudgets();
	
}
