package com.expensetracker.controller;

import java.util.Collections;
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

import com.expensetracker.model.Expense;
import com.expensetracker.service.ExpenseService;

@RestController

public class ExpenseController {

    
	
	@Autowired
	private ExpenseService expenseService ;

    

	@GetMapping("/expense")
	public String getExpenses() {
		return "this is expeses get page";
	}
	@PostMapping("/expense")
	public ResponseEntity<String> addExpense(@RequestBody Expense expense){
		try {
			Expense expense1= expenseService.addExpense(expense);
			if(expense1==null)
				return new ResponseEntity<>("Insuffiecent Data",HttpStatus.NO_CONTENT);
			return new ResponseEntity<>("Successfully added!",HttpStatus.CREATED);
			
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Internall server error",HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	@PutMapping("/expense/{id}")
	public ResponseEntity<String> updateExpense(@PathVariable Long id, @RequestBody Expense expense){
		try {
			ServiceResponse<String> response=expenseService.updateExpnse(id,expense);
			if(!response.isStatus()) 
				return new ResponseEntity<>(response.getData(),HttpStatus.NOT_FOUND);
			return new ResponseEntity<>(response.getData(),HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Internall server error",HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	@DeleteMapping("/expense/{id}")
	public ResponseEntity<String> removeExpense(@PathVariable Long id){
		try {
			ServiceResponse<String> response=expenseService.removeExpense(id);
			if(!response.isStatus()) 
				return new ResponseEntity<>(response.getData(),HttpStatus.NOT_FOUND);
			return new ResponseEntity<>(response.getData(),HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>("Internall server error",HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	@GetMapping("/expense/{id}")
	public ResponseEntity<Optional<Expense>> findExpense(@PathVariable Long id){
		try {
			ServiceResponse<Optional<Expense>> response=expenseService.getExpense(id);
			if(!response.isStatus()) 
				return new ResponseEntity<>(response.getData(),HttpStatus.NOT_FOUND);
			return new ResponseEntity<>(response.getData(),HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(Optional.empty(),HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	@GetMapping("/expenses")
	public ResponseEntity<List<Expense>> findExpense(){
		try {
			ServiceResponse<List<Expense>> response=expenseService.getAllExpenses();
			return new ResponseEntity<>(response.getData(),HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(Collections.emptyList(),HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
}
