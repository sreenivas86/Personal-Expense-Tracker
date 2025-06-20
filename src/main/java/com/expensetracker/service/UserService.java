package com.expensetracker.service;

import com.expensetracker.model.User;

public interface UserService {
	
	public boolean register(User user); 
	public boolean userExists(String email);
	public boolean login(User user);
	

}
