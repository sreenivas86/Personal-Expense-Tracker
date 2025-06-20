package com.expensetracker.model;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import lombok.Data;

@Entity
@Data
@JsonIgnoreProperties({ "expenses","budgets" })
public class Category {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long categoryId;
	@Column(nullable = false, unique = true)
	private String name;
	private String colorCode;
	private LocalDateTime createdAt;

	@OneToMany(mappedBy = "category", cascade = CascadeType.ALL)

	private List<Expense> expenses;
	
	@OneToMany(mappedBy = "category", cascade = CascadeType.ALL)

	private List<Expense> budgets;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

}
