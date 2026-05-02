package com.foodzie.controller;

import com.foodzie.dto.AuthResponse;
import com.foodzie.dto.LoginUserDTO;
import com.foodzie.dto.UserDTO;
import com.foodzie.model.User;
import com.foodzie.service.UserService;
import com.foodzie.utilities.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class UserController {

    private final UserService userService;
    private final ModelMapper modelMapper;
    private final CookieUtil cookieUtil;

    public UserController(
            UserService userService,
            ModelMapper modelMapper,
            CookieUtil cookieUtil
    ) {
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.cookieUtil = cookieUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody User user) {
        User saved = userService.registerUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(modelMapper.map(saved, UserDTO.class));
    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(
            @Valid @RequestBody LoginUserDTO user,
            HttpServletResponse response
    ) {
        AuthResponse auth = userService.loginUser(user);
        cookieUtil.setTokenCookies(response, auth.getAccessToken(), auth.getRefreshToken());
        return ResponseEntity.ok(auth.getUser());
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<UserDTO> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtil.extractCookie(request, "refreshToken");
        AuthResponse auth = userService.refreshToken(refreshToken);
        cookieUtil.setTokenCookies(response, auth.getAccessToken(), auth.getRefreshToken());
        return ResponseEntity.ok(auth.getUser());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        cookieUtil.clearTokenCookies(response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user-info")
    public ResponseEntity<UserDTO> getUserInfo(HttpServletRequest request) {
        String accessToken = cookieUtil.extractCookie(request, "accessToken");
        return ResponseEntity.ok(userService.getUserInfo(accessToken));
    }

}
