package com.biosteel.teams.auth.service;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biosteel.teams.auth.dto.BearerTokenDto;
import com.biosteel.teams.user.model.User;
import com.biosteel.teams.user.repository.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

	private final UserRepository userRepository;

	@Value("${users.security.token.secret}")
	private String secret;

	@Value("${users.security.token.accessTokenExpirationInSeconds}")
	private long accessTokenExpirationInSeconds;

	@Value("${users.security.token.refreshTokenExpirationInSeconds}")
	private long refreshTokenExpirationInSeconds;

	private static final String AUDIENCE = "BioSteel";
	private static final String ROLES_CLAIM = "roles";

	@Transactional
	public BearerTokenDto generateNewToken(final User user) {
		log.debug("TokenService.generateNewToken: called with user: {}", user);

		Date now = new Date();
		Date accessTokenExpiry = new Date(now.getTime() + accessTokenExpirationInSeconds * 1000);
		Date refreshTokenExpiry = new Date(now.getTime() + refreshTokenExpirationInSeconds * 1000);

		String accessToken = generateToken(user, now, accessTokenExpiry);
		String refreshToken = generateToken(user, now, refreshTokenExpiry);

		BearerTokenDto token = new BearerTokenDto();
		token.setAccessToken(accessToken);
		token.setRefreshToken(refreshToken);
		token.setExpiryDate(accessTokenExpiry);

		// Save tokens to user entity
		user.setAccessToken(accessToken);
		user.setRefreshToken(refreshToken);
		user.setTokenExpiryDate(
				accessTokenExpiry.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
		userRepository.save(user);

		log.debug("TokenService.generateNewToken: tokens generated and saved: {}", token);
		return token;
	}

	private String generateToken(User user, Date issuedAt, Date expiration) {
		Map<String, Object> claims = new HashMap<>();
		claims.put(Claims.AUDIENCE, AUDIENCE);
		claims.put(Claims.SUBJECT, user.getEmail());
		claims.put(ROLES_CLAIM, user.getRoles().stream()
				.map(role -> role.getName())
				.collect(Collectors.toList()));

		SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

		return Jwts.builder()
				.setClaims(claims)
				.setIssuedAt(issuedAt)
				.setExpiration(expiration)
				.signWith(key)
				.compact();
	}

	public boolean validateToken(String token, String appUuid) {
		try {
			SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

			Claims claims = Jwts.parserBuilder()
					.setSigningKey(key)
					.requireAudience(AUDIENCE)
					.build()
					.parseClaimsJws(token)
					.getBody();

			// Check expiration
			if (claims.getExpiration().before(new Date())) {
				log.debug("Token validation failed: token is expired");
				return false;
			}

			// Check if token exists in user record
			String userEmail = claims.getSubject();
			User user = userRepository.findByEmailIgnoreCase(userEmail)
					.orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

			if (!token.equals(user.getAccessToken()) && !token.equals(user.getRefreshToken())) {
				log.debug("Token validation failed: token not found in user record");
				return false;
			}

			return true;
		} catch (JwtException | IllegalArgumentException e) {
			log.warn("Token validation failed: {}", e.getMessage());
			return false;
		}
	}

	public String getUserEmailFromToken(String token) {
		try {
			SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

			return Jwts.parserBuilder()
					.setSigningKey(key)
					.build()
					.parseClaimsJws(token)
					.getBody()
					.getSubject();
		} catch (JwtException e) {
			log.warn("Failed to extract user email from token: {}", e.getMessage());
			throw new JwtException("Invalid token");
		}
	}

	public Collection<? extends GrantedAuthority> getUserAuthorities(String userEmail, String appUuid) {
		User user = userRepository.findByEmailIgnoreCase(userEmail)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

		return user.getRoles().stream()
				.map(role -> new SimpleGrantedAuthority(role.getName()))
				.collect(Collectors.toList());
	}

	public User getUserFromToken(String token, String appUuid) {
		String userEmail = getUserEmailFromToken(token);
		return userRepository.findByEmailIgnoreCase(userEmail)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));
	}

	@Transactional
	public BearerTokenDto refreshToken(String refreshToken, String appUuid) {
		if (!validateToken(refreshToken, appUuid)) {
			throw new JwtException("Invalid refresh token");
		}

		String userEmail = getUserEmailFromToken(refreshToken);
		User user = userRepository.findByEmailIgnoreCase(userEmail)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

		// Invalidate old refresh token
		user.setRefreshToken(null);
		userRepository.save(user);

		return generateNewToken(user);
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		try {
			SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

			Claims claims = Jwts.parserBuilder()
					.setSigningKey(key)
					.requireAudience(AUDIENCE)
					.build()
					.parseClaimsJws(token)
					.getBody();

			// Check if token is expired
			if (claims.getExpiration().before(new Date())) {
				log.debug("Token validation failed: token is expired");
				return false;
			}

			// Verify the token's subject matches the UserDetails username
			String tokenUsername = claims.getSubject();
			if (!tokenUsername.equals(userDetails.getUsername())) {
				log.debug("Token validation failed: username mismatch");
				return false;
			}

			return true;
		} catch (JwtException | IllegalArgumentException e) {
			log.warn("Token validation failed: {}", e.getMessage());
			return false;
		}
	}

	public boolean isTokenExpiringSoon(Date expiryDate) {
		final long FIVE_MINUTES_IN_MS = 5 * 60 * 1000;
		return expiryDate != null &&
				expiryDate.before(new Date(System.currentTimeMillis() + FIVE_MINUTES_IN_MS));
	}

	@Transactional
	public void invalidateUserTokens(User user) {
		user.setAccessToken(null);
		user.setRefreshToken(null);
		user.setTokenExpiryDate(null);
		userRepository.save(user);
	}
}