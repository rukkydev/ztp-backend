package com.ztp.auth;

import com.ztp.common.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class CsrfController {

    @GetMapping("/csrf-token")
    public ApiResponse<Map<String, String>> getCsrfToken(CsrfToken csrfToken, HttpServletResponse response) {
        String token = csrfToken != null ? csrfToken.getToken() : "";
        String headerName = csrfToken != null ? csrfToken.getHeaderName() : "X-XSRF-TOKEN";
        String paramName = csrfToken != null ? csrfToken.getParameterName() : "_csrf";

        if (token != null && !token.isBlank()) {
            response.setHeader("XSRF-TOKEN", token);
            response.setHeader("X-XSRF-TOKEN", token);
        }

        return ApiResponse.success("CSRF token ready", Map.of(
            "token", token,
            "headerName", headerName,
            "parameterName", paramName
        ));
    }
}