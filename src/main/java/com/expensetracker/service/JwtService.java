package com.expensetracker.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
	
	public String generateToken(String username);
	
	public String extractUsername(String token);
	public boolean validToken(String token,UserDetails userDetails)throws Exception;

}
