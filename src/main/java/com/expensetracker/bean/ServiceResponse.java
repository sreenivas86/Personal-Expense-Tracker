package com.expensetracker.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class used only for save record and update record only
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ServiceResponse <T> {
	
	private boolean status;
	private T data;
	

}
