package com.ztp.auth;

import com.ztp.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CsrfController {

    @GetMapping("/csrf-token")
    public ApiResponse<Void> getCsrfToken() {
        return ApiResponse.success("CSRF cookie set", null);
    }
}