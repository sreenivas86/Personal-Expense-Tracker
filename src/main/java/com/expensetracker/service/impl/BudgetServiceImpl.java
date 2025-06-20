package com.expensetracker.service.impl;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.expensetracker.bean.ServiceResponse;
import com.expensetracker.model.Budget;
import com.expensetracker.model.Category;
import com.expensetracker.model.User;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.UserUtil;
import com.expensetracker.service.BudgetService;
@Service
public class BudgetServiceImpl implements BudgetService{
	@Autowired
	private UserUtil userUtil;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired
	private BudgetRepository budgetRepository;
	
	
	@Override
	public ServiceResponse<String> addBudjet(Budget budget) {
		Long categoryId =budget.getCategory().getCategoryId();
		String username=userUtil.getCurrentUsername();
		if(categoryId==null)
			return new ServiceResponse<>(false, "Category id is required");
		Optional<Category> category=categoryRepository.findById(categoryId);
		if(category.isEmpty())
			return new ServiceResponse<>(false, "Category id is not found");
		if(budget.getMonth()==null)
			return new ServiceResponse<>(false, "month  id is required");
		
		Optional<Budget> existBudget= budgetRepository.findByMonthAndCategory_CategoryIdAndUser_Email(budget.getMonth(), categoryId,username);
		if(!existBudget.isEmpty())
			return new ServiceResponse<>(false, "budget is alredy allocated");
		User user=userRepository.findByEmail(userUtil.getCurrentUsername());
		budget.setCategory(category.get());
		budget.setUser(user);
		budgetRepository.save(budget);
		return new ServiceResponse<>(true, "budget saved");
	}

	@Override
	public ServiceResponse<String> updateBudget(Long id, Budget budget) {
		if(!budgetRepository.existsById(id))
			return new ServiceResponse<>(false,"budget id not found");
		Long categoryId =budget.getCategory().getCategoryId();
		if(categoryId==null)
			return new ServiceResponse<>(false, "Category id is required");
		Optional<Category> category=categoryRepository.findById(categoryId);
		if(category.isEmpty())
			return new ServiceResponse<>(false, "Category id is not found");
		if(budget.getMonth()==null)
			return new ServiceResponse<>(false, "month  id is required");
		Optional<Budget> b = Optional.empty();
		if(id!=null) {
			b=budgetRepository.findById(id);
			System.out.println(b);
			if(b.isEmpty())
				return new ServiceResponse<>(false, "budget id not found");
			
		}
		
		User user=userRepository.findByEmail(userUtil.getCurrentUsername());
		Budget exBudget=b.get();
		exBudget.setAmount(budget.getAmount());
		exBudget.setCategory(category.get());
		exBudget.setMonth(budget.getMonth());
		exBudget.setUser(user);
		
		budgetRepository.save(exBudget);
		return new ServiceResponse<>(true, "budget saved");
		
	}

	@Override
	public ServiceResponse<String> deleteBudget(Long id) {
		if(!budgetRepository.existsById(id))
			return new ServiceResponse<>(false,"budget id not found");
		budgetRepository.deleteById(id);
		return new ServiceResponse<>(true,"budget is Deleted");
	}

	@Override
	public ServiceResponse<Optional<Budget>> findBudget(Long id) {
		Optional<Budget> budget=budgetRepository.findById(id);
		
		if(budget.isEmpty())
			return new ServiceResponse<>(false,budget);
		
		
		return new ServiceResponse<>(true,budget);
		
	}

	@Override
	public ServiceResponse<List<Budget>> findBudgets() {
		String username=userUtil.getCurrentUsername();
		List<Budget> budgets=budgetRepository.findBudgetsByUserEmail(username);
		return new ServiceResponse<>(true,budgets);
	}

}
