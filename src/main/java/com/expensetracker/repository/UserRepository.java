package com.expensetracker.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import com.expensetracker.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
	public User findByEmail(String email);

}
