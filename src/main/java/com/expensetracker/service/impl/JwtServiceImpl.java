package com.expensetracker.service.impl;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.expensetracker.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtServiceImpl implements JwtService {
	private static final long EXPIRATION_TIME=1000*60*60;
	private static final String SECRET = "sresra-enivan-vasi-srasre-vaneni-ivas";
	private static final Key SECRETE_KEY =Keys.hmacShaKeyFor(SECRET.getBytes());

	@Override
	public String generateToken(String username) {
		
		return Jwts.builder()
				.subject(username)
				.issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis()+EXPIRATION_TIME))
				.signWith(SECRETE_KEY)
				.compact();
	}

	@Override
	public String extractUsername(String token) {
		return extractClaim(token,Claims::getSubject);
	}

	private <T> T extractClaim(String token, Function<Claims, T> claimResolver ) {
		final Claims claims=extractAllClaims( token);
		return claimResolver.apply(claims);
	}

	private Claims extractAllClaims(String token) {
		try {
			return Jwts.parser()
					.verifyWith( (SecretKey) SECRETE_KEY)
					.build()
					.parseSignedClaims(token)
					.getPayload();
		} catch (Exception e) {
			throw new RuntimeException("invalic Jwt token", e);
		}
	}

	@Override
	public boolean validToken(String token, UserDetails userDetails) throws Exception {
		
			final String username=extractUsername(token);
			return (username.equals(userDetails.getUsername()));
		
	}
	
	

}
