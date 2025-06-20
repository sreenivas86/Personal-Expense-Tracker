package com.expensetracker.cofig;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.expensetracker.filter.JwtFilter;
import com.expensetracker.service.impl.UserServiceImpl;

@Configuration
@EnableWebSecurity
public class AppConfig {
	
	private UserServiceImpl userServiceImpl;
	
	@Autowired
	private JwtFilter jwtFilter;

	public AppConfig(@Lazy UserServiceImpl userServiceImpl) {
		super();
		this.userServiceImpl = userServiceImpl;
	}
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	AuthenticationManager authManager(HttpSecurity http) throws Exception {
		AuthenticationManagerBuilder auth = http.getSharedObject(AuthenticationManagerBuilder.class);
		auth.userDetailsService(userServiceImpl).passwordEncoder(passwordEncoder());
		return auth.build();
				
	}
	
//	@Bean 
//	DaoAuthenticationProvider authProvider() {
//		DaoAuthenticationProvider auth=new DaoAuthenticationProvider(userServiceImpl);
//		auth.setPasswordEncoder(passwordEncoder());
//		return auth;
//	}
	
	
	
//	@Bean 
//	AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {	
//		return config.getAuthenticationManager();
//	}

	@Bean
	SecurityFilterChain security(HttpSecurity http) throws Exception {

		http.authorizeHttpRequests(req -> {
			req.requestMatchers("/auth/**","/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html").permitAll().anyRequest().authenticated();
		});
		http.csrf(csrf -> csrf.disable());
		http.addFilterBefore(corsFilter(),UsernamePasswordAuthenticationFilter.class);
		http.addFilterBefore(jwtFilter,UsernamePasswordAuthenticationFilter.class);
		
		// http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
	
	@Bean
	CorsFilter  corsFilter() {
		CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173")); // frontend origin
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // 

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source); 
	}
	
	
	

}
