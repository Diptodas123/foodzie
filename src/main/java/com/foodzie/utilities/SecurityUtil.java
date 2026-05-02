package com.foodzie.utilities;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SecurityUtil {

    private SecurityUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static String sanitize(String s) {
        if (s == null) {
            return "";
        }
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
