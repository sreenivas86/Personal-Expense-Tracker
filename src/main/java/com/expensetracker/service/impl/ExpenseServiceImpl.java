package com.expensetracker.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.expensetracker.bean.ServiceResponse;
import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;
import com.expensetracker.model.User;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.UserUtil;
import com.expensetracker.service.ExpenseService;

@Service
public class ExpenseServiceImpl implements ExpenseService {
	@Autowired
	private ExpenseRepository expenseRepo;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private CategoryRepository categoryRepo;
	@Autowired
	private UserUtil userUtil;
	

	@Transactional
	@Override
	public Expense addExpense(Expense expense) {
		User user=userRepository.findByEmail(userUtil.getCurrentUsername());
		System.out.println(expense);
		System.out.println(expense.getCategory().getCategoryId());
		Category category=categoryRepo.findById(expense.getCategory().getCategoryId()).get();
		expense.setUser(user);
		expense.setCategory(category);
		Expense newExpense=expenseRepo.save(expense);	
		return newExpense;
	}

	@Transactional
	@Override
	public ServiceResponse<String> updateExpnse(Long id,Expense expense) {
		
		Optional<Expense> existingExpense=expenseRepo.findById(id);
		if(existingExpense.isEmpty())
			return new ServiceResponse<>(false,"Expense not found");
		Optional<Category> existingCategory=categoryRepo.findById(expense.getCategory().getCategoryId());
		if(existingCategory.isEmpty())
			return new ServiceResponse<>(false ,"Catergory not found");
		Expense saveExpense=existingExpense.get();
		saveExpense.setTitle(expense.getTitle());
		saveExpense.setNote(expense.getNote());
		saveExpense.setCategory(existingCategory.get());
		saveExpense.setAmount(expense.getAmount());
		expenseRepo.save(saveExpense);
		
		return new ServiceResponse<>(true ,"updated successfully");
		
		
	}

	@Override
	public ServiceResponse<Optional<Expense>> getExpense(Long id) {
		if(!isExists(id))
			return new ServiceResponse<>(false ,Optional.empty());
		return new ServiceResponse<>(true,expenseRepo.findById(id));
	}

	@Override
	public ServiceResponse<List<Expense>> getAllExpenses() {
		
		List<Expense> expenses=expenseRepo.findAllByUserEmail(userUtil.getCurrentUsername());
		
		return new ServiceResponse<>( true ,expenses) ;
	}

	@Override
	public boolean isExists(Long id) {
		return expenseRepo.existsById(id);
	}

	@Transactional
	@Override
	public ServiceResponse<String> removeExpense(Long id) {
		if(!isExists(id))
			return new ServiceResponse<>(false ,"expense not found");
		expenseRepo.deleteById(id);
		 return new ServiceResponse<>( true ,"expense removed successfully") ;
		
	}

}
