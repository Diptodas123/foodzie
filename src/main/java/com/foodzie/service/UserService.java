package com.foodzie.service;

import com.foodzie.dto.AuthResponse;
import com.foodzie.dto.LoginUserDTO;
import com.foodzie.dto.UserDTO;
import com.foodzie.exception.InvalidCredentialsException;
import com.foodzie.exception.InvalidTokenException;
import com.foodzie.exception.UserAlreadyExistsException;
import com.foodzie.model.User;
import com.foodzie.repository.UserRepository;
import com.foodzie.utilities.JwtUtil;
import com.foodzie.utilities.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final ModelMapper modelMapper;

    private final static String ACCESS = "access";
    private final static String REFRESH = "refresh";

    public UserService(UserRepository userRepository, JwtUtil jwtUtil, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.modelMapper = modelMapper;
    }

    public User registerUser(User user) {
        log.info("Registering user with email: {}", SecurityUtil.sanitize(user.getEmail()));
        if (validateUserIfExists(user)) {
            throw new UserAlreadyExistsException("User with the same username or email already exists.");
        }
        user.setPassword(hashPassword(user.getPassword()));
        User saved = userRepository.save(user);
        log.info("User registered successfully with id: {}",
                SecurityUtil.sanitize(saved.getId().toString())
        );
        return saved;
    }

    public AuthResponse loginUser(LoginUserDTO user) {
        log.info("Login attempt for email: {}", SecurityUtil.sanitize(user.getEmail()));
        User existingUser = userRepository.findByEmail(user.getEmail()).orElse(null);
        if (existingUser == null || !comparePassword(user.getPassword(), existingUser.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password.");
        }
        UserDTO userDTO = modelMapper.map(existingUser, UserDTO.class);
        return new AuthResponse(
                jwtUtil.generateAccessToken(userDTO),
                jwtUtil.generateRefreshToken(userDTO),
                userDTO
        );
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            log.warn("Refresh token validation failed");
            throw new InvalidTokenException("Refresh token is invalid or expired.");
        }
        if (!REFRESH.equals(jwtUtil.extractTokenType(refreshToken))) {
            throw new InvalidTokenException("Provided token is not a refresh token.");
        }
        String email = jwtUtil.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException("User not found for this token."));
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        return new AuthResponse(
                jwtUtil.generateAccessToken(userDTO),
                jwtUtil.generateRefreshToken(userDTO),
                userDTO
        );
    }

    private boolean validateUserIfExists(User user) {
        return userRepository.existsByUserName(user.getUserName()) || userRepository.existsByEmail(user.getEmail());
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private boolean comparePassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    public UserDTO getUserInfo(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new InvalidTokenException("Token is invalid or expired.");
        }
        if (!ACCESS.equals(jwtUtil.extractTokenType(token))) {
            throw new InvalidTokenException("Only access tokens are allowed here.");
        }
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException("User not found for this token."));
        return modelMapper.map(user, UserDTO.class);
    }
}
