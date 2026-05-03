package com.foodzie.controller;

import com.foodzie.dto.AuthResponse;
import com.foodzie.dto.LoginUserDTO;
import com.foodzie.dto.UserDTO;
import com.foodzie.utilities.ResponseUtil;
import com.foodzie.model.User;
import com.foodzie.service.UserService;
import com.foodzie.utilities.Constants;
import com.foodzie.utilities.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class UserController {

    private final UserService userService;
    private final CookieUtil cookieUtil;
    private final ResponseUtil responseUtil;

    public UserController(
            UserService userService,
            CookieUtil cookieUtil,
            ResponseUtil responseUtil
    ) {
        this.userService = userService;
        this.cookieUtil = cookieUtil;
        this.responseUtil = responseUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        User saved = userService.registerUser(user);
        return responseUtil.buildResponse(
                saved,
                UserDTO.class,
                "User registered successfully",
                HttpStatus.CREATED
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginUserDTO user,
            HttpServletResponse response
    ) {
        AuthResponse auth = userService.loginUser(user);
        cookieUtil.setTokenCookies(response, auth.getAccessToken(), auth.getRefreshToken());
        return responseUtil.buildResponse(auth.getUser(), "Login successful");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtil.extractCookie(request, Constants.REFRESH_TOKEN);
        AuthResponse auth = userService.refreshToken(refreshToken);
        cookieUtil.setTokenCookies(response, auth.getAccessToken(), auth.getRefreshToken());
        return responseUtil.buildResponse(auth.getUser(), "Token refreshed successfully");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        cookieUtil.clearTokenCookies(response);
        return responseUtil.buildResponse("Logged out successfully");
    }

    @GetMapping("/user-info")
    public ResponseEntity<?> getUserInfo(HttpServletRequest request) {
        String accessToken = cookieUtil.extractCookie(request, Constants.ACCESS_TOKEN);
        return responseUtil.buildResponse(
                userService.getUserInfo(accessToken),
                "User info retrieved successfully"
        );
    }
}
