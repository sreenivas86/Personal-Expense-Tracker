package com.expensetracker.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.expensetracker.bean.ServiceResponse;
import com.expensetracker.model.Budget;
import com.expensetracker.service.BudgetService;

@RestController
public class BudgetController {
	@Autowired
	private BudgetService budgetService;

	@PostMapping("/budget")
	public ResponseEntity<String> addBuget(@RequestBody Budget b) {
		ServiceResponse<String>  response= budgetService.addBudjet(b);
		if(!response.isStatus())
			return new ResponseEntity<String>(response.getData(),HttpStatus.BAD_REQUEST);
		
		return new ResponseEntity<String>(response.getData(),HttpStatus.CREATED);
		
	}
	@PutMapping("/budget/{id}")
	public ResponseEntity<String> upadateBuget(@PathVariable Long id,@RequestBody Budget b) {
		System.out.println(b.getCategory().getCategoryId());
		ServiceResponse<String>  response= budgetService.updateBudget(id, b);
		if(!response.isStatus())
			return new ResponseEntity<String>(response.getData(),HttpStatus.BAD_REQUEST);
		
		return new ResponseEntity<String>(response.getData(),HttpStatus.OK);
		
	}
	@DeleteMapping("/budget/{id}")
	public ResponseEntity<String> deleteBuget(@PathVariable Long id) {
		ServiceResponse<String>  response= budgetService.deleteBudget(id);
		if(!response.isStatus())
			return new ResponseEntity<String>(response.getData(),HttpStatus.BAD_REQUEST);
		
		return new ResponseEntity<String>(response.getData(),HttpStatus.OK);
		
	}
	
	@GetMapping("/budget/{id}")
	public ResponseEntity<Optional<Budget>> getBuget(@PathVariable Long id) {
		ServiceResponse<Optional<Budget>>  response= budgetService.findBudget(id);
		if(!response.isStatus())
			return new ResponseEntity<>(response.getData(),HttpStatus.BAD_REQUEST);
		
		return new ResponseEntity<>(response.getData(),HttpStatus.OK);
		
	}
	@GetMapping("/budgets")
	public ResponseEntity<List<Budget>> getBugets() {
		ServiceResponse<List<Budget>>  response= budgetService.findBudgets();
		if(!response.isStatus())
			return new ResponseEntity<>(response.getData(),HttpStatus.BAD_REQUEST);
		
		return new ResponseEntity<>(response.getData(),HttpStatus.OK);
		
	}
	 
}
