package com.expensetracker.repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.expensetracker.model.Budget;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

	//Optional<Budget> findByMonthAndCategory_CategoryId(YearMonth month, Long categoryId);
	Optional<Budget> findByMonthAndCategory_CategoryIdAndUser_Email(YearMonth month, Long categoryId, String email);

	public List<Budget> findBudgetsByUserEmail(String email);
}
