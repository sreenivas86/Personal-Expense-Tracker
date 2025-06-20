package com.expensetracker.service.impl;



import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.expensetracker.model.User;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.service.UserService;

@Service
public class UserServiceImpl implements UserService,UserDetailsService {
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	public UserServiceImpl(@Lazy AuthenticationManager authManager) {
		super();
		this.authManager = authManager;
	}

	private AuthenticationManager authManager;

	@Override
	public boolean register(User user) {
		String epwd=passwordEncoder.encode(user.getPassword());
		user.setPassword(epwd);
		User sUser=userRepository.save(user);
		return sUser.getUserId()!=null;
	}

	@Override
	public boolean userExists(String email) {

		User eUser =userRepository.findByEmail(email);
		
		
		return eUser!=null;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User euser=userRepository.findByEmail(email);
		if(euser==null) {
			throw new RuntimeException("User not found "+email);
		}
		return new org.springframework.security.core.userdetails.User(euser.getEmail(),euser.getPassword(),new ArrayList<>());
	}

	@Override
	public boolean login(User user) {
		UsernamePasswordAuthenticationToken toke=
				new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword());
		Authentication auth=authManager.authenticate(toke);
		
		return auth.isAuthenticated();
	}

	
}
