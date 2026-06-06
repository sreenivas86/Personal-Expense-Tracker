package com.expensetracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expensetracker.model.User;
import com.expensetracker.service.JwtService;
import com.expensetracker.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@Tag(name="Authenticatio Management", description = "APIs for managing Authentication ")
@SecurityRequirement(name="No auth " , scopes="direct access is ")
public class UserController {

	@Autowired
	private UserService userService;
	
	@Autowired 
	private JwtService jwtService;
	
	
	@Operation(
			summary = "login user",
			responses = {
					@ApiResponse(responseCode = "400", description = "login successfully return jwt token" ),
					@ApiResponse(responseCode = "400", description = "Invalid input")
			}
			)
	
	@PostMapping("/login")
	public ResponseEntity<String> login(@RequestBody User user){
		
		try {
			if(!userService.login(user)) {
				throw new RuntimeException();
			}
			return new ResponseEntity<>(jwtService.generateToken(user.getEmail()),HttpStatus.OK);
			
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
		}
	}
	
	@Operation(
		    summary = "User Registration ",
		    responses = {
		        @ApiResponse(responseCode = "201", description = "User registered successfully"),
		        @ApiResponse(responseCode = "500", description = "Internal server errror"),
		        @ApiResponse(responseCode = "409", description = "User email already exists")
		    }
		)

	@PostMapping("/register")
	public ResponseEntity<String> registerUser(@RequestBody User user) {
		if (userService.userExists(user.getEmail()))
			return new ResponseEntity<String>("Email already exists", HttpStatus.CONFLICT);
		try {

			if (!userService.register(user))
				throw new RuntimeException();

			return new ResponseEntity<String>("register successfully", HttpStatus.CREATED);

		} catch (Exception e) {
			return new ResponseEntity<String>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	@GetMapping("/welcome")
	public String authWelcome() {
		return "sreenivas is developer file wat ";
	}
}
