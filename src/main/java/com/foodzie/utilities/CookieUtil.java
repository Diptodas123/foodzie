package com.foodzie.utilities;

import com.foodzie.exception.InvalidTokenException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class CookieUtil {

    private final boolean secure;

    public CookieUtil(
            @Value("${app.cookie.secure:true}") boolean secure
    ) {
        this.secure = secure;
    }

    public void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        response.addCookie(buildCookie("accessToken", accessToken, 3600));      // 1 hour
        response.addCookie(buildCookie("refreshToken", refreshToken, 604800));  // 7 days
    }

    public void clearTokenCookies(HttpServletResponse response) {
        response.addCookie(buildCookie("accessToken", "", 0));
        response.addCookie(buildCookie("refreshToken", "", 0));
    }

    public Cookie buildCookie(String name, String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        return cookie;
    }

    public String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            throw new InvalidTokenException("No cookies found in request.");
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> name.equals(cookie.getName()))
                .findFirst()
                .orElseThrow(() -> new InvalidTokenException("'" + name + "' cookie not found."))
                .getValue();
    }
}
