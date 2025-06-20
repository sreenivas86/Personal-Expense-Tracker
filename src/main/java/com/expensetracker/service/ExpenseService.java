package com.expensetracker.service;

import java.util.List;
import java.util.Optional;


import com.expensetracker.bean.ServiceResponse;
import com.expensetracker.model.Expense;

public interface ExpenseService {
	public Expense addExpense(Expense expense) ;
	public ServiceResponse<String> updateExpnse(Long id,Expense expense);
	public ServiceResponse<Optional<Expense>> getExpense(Long id);
	public ServiceResponse<List<Expense>> getAllExpenses();
	public boolean isExists(Long id);
	public ServiceResponse<String> removeExpense(Long id);
}
