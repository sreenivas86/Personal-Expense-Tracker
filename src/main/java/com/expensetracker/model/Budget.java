package com.expensetracker.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
@JsonIgnoreProperties({"user"})
public class Budget {

	@Id
	@GeneratedValue
	private Long budgetId;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	@ToString.Exclude
	private User user;

	@ManyToOne
	@JoinColumn(name = "category_id", nullable = false)
	@ToString.Exclude
	private Category category;

	private BigDecimal amount;

	private YearMonth month;

	private LocalDateTime createdAt;
	

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

}
