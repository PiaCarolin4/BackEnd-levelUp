package com.levelup.auth.service;


import com.levelup.auth.config.jwt.JwtUtils;
import com.levelup.auth.dto.AuthResponse;
import com.levelup.auth.dto.LoginRequest;
import com.levelup.auth.dto.RegisterRequest;


import com.levelup.auth.model.Role;
import com.levelup.auth.model.User;
import com.levelup.auth.repository.RoleRepository;
import com.levelup.auth.repository.UserRepository;
import com.levelup.auth.service.exception.UserAlreadyExistsException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.management.relation.RoleNotFoundException;
import java.security.SignatureException;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;


@Service

public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtils jwtService;
    @Autowired
    private  PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Value("${auth.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            UserDetails user = (UserDetails) authentication.getPrincipal();

            Date issuedAt = new Date();
            Date expiration = new Date(System.currentTimeMillis() + jwtExpirationMs);
            String token = jwtService.getToken(user);
            User userFind= userRepository.findByUsername(user.getUsername())
                    .orElseThrow(()-> new RuntimeException("Usuario no encontrado"));

            return AuthResponse.builder()
                    .issuedAt(issuedAt)
                    .expiresAt(expiration)
                    .token(token)
                    .roles(userFind.getRoles())
                    .username(userFind.getUsername())
                    .build();

        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid username or password"
            );
        }
    }

    public AuthResponse register(RegisterRequest request) {

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));

        User user= User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Collections.singleton(userRole))

                .enabled(true)
                .build();

        userRepository.save(user);
        Date issuedAt = new Date();
        Date expiration = new Date(System.currentTimeMillis() + jwtExpirationMs);
        String token = jwtService.getToken(user);

        return AuthResponse.builder()
                .issuedAt(issuedAt)
                .expiresAt(expiration)
                .token(token)
                .build();
    }
    public User registerUser(RegisterRequest userRequest) {
        
        if (userRequest == null) {
            throw new IllegalArgumentException("RegisterRequest cannot be null");
        }

        Role roleDefault = null;
        try {
            roleDefault = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RoleNotFoundException("ROLE_USER not found"));
        } catch (RoleNotFoundException e) {
            throw new RuntimeException(e);
        }

        return User.builder()
                .username(userRequest.getUsername())
                .email(userRequest.getEmail())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .roles(Collections.singleton(roleDefault))
                .locked(false)
                .enabled(true)
                .build();
    }

    public AuthResponse createUser(RegisterRequest request) {
        Objects.requireNonNull(request, "RegisterRequest cannot be null");
        validateUserDoesNotExist(request.getUsername(), request.getEmail());
        try {
            User savedUser = registerAndSaveUser(request);
            return buildAuthResponse(savedUser);

        } catch (JwtException e) {
            logger.error("JWT generation failed for user: {}", request.getUsername(), e);
            throw new ServiceException("Registration failed: could not generate access token", e);
        } catch (DataAccessException e) {
            logger.error("Database error during user registration for: {}", request.getUsername(), e);
            throw new ServiceException("Registration failed: database error", e);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during user registration", e);
            throw new ServiceException("Registration failed due to unexpected error", e);
        }
    }

    private void validateUserDoesNotExist(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Username already exists: " + username);
        }

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email already exists: " + email);
        }
    }

    private User registerAndSaveUser(RegisterRequest request) {
        User user = registerUser(request);
        return userRepository.save(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        Date issuedAt = new Date();
        Date expiration = new Date(System.currentTimeMillis() + jwtExpirationMs);
        String token = jwtService.getToken(user);

        return AuthResponse.builder()
                .token(token)
                .issuedAt(issuedAt)
                .expiresAt(expiration)
                .username(user.getUsername())
                .roles(user.getRoles())
                .build();
    }

    public void validateToken(String token) {
        try {
            jwtService.validateToken(token); 
        } catch (ExpiredJwtException e) {
            throw new BadCredentialsException("Token expirado");
        } catch (MalformedJwtException e) {
            throw new BadCredentialsException("Token inválido");
        } catch (IllegalArgumentException e) {
            throw new BadCredentialsException("Token vacío o mal formado");
        }
    }

}
