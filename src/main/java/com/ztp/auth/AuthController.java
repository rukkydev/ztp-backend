package com.ztp.auth;

import com.ztp.auth.dto.LoginRequest;
import com.ztp.auth.dto.LoginResponse;
import com.ztp.common.ApiResponse;
import com.ztp.user.User;
import com.ztp.user.UserService;
import com.ztp.user.dto.RegisterRequest;
import com.ztp.user.dto.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import com.ztp.user.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import com.ztp.auth.dto.TwoFactorVerifyRequest;
import com.ztp.auth.dto.VerifyDeviceRequest;
import com.ztp.auth.dto.ResendCodeRequest;
import com.ztp.auth.dto.ForgotPasswordRequest;
import com.ztp.auth.dto.ResetPasswordRequest;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final CsrfTokenRepository csrfTokenRepository;
	private final UserRepository userRepository;
	private final com.ztp.auth.reset.PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        User createdUser = userService.registerUser(request);
        UserResponse response = new UserResponse(createdUser);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request,
                                                          HttpServletRequest httpRequest,
                                                          HttpServletResponse httpResponse) {
    LoginResponse response = authService.login(request, httpRequest, httpResponse);
    return ResponseEntity.ok(ApiResponse.success("Login successful", response));
}

@PostMapping("/logout")
public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
    HttpSession session = httpRequest.getSession(false);
    if (session != null) {
        session.invalidate();
    }
    SecurityContextHolder.clearContext();

    // Also rotate the CSRF token, so a stale token from the previous
    // session/user can't be reused going forward.
    csrfTokenRepository.saveToken(null, httpRequest, httpResponse);

    return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
}

@PostMapping("/2fa/verify")
public ResponseEntity<ApiResponse<LoginResponse>> verifyTwoFactor(@Valid @RequestBody TwoFactorVerifyRequest request,
                                                                    HttpServletRequest httpRequest,
                                                                    HttpServletResponse httpResponse) {
    LoginResponse response = authService.verifyTwoFactor(request, httpRequest, httpResponse);
    return ResponseEntity.ok(ApiResponse.success("Login successful", response));
}

@PostMapping("/2fa/resend")
public ResponseEntity<ApiResponse<Void>> resendTwoFactorCode(@Valid @RequestBody ResendCodeRequest request) {
    authService.resendTwoFactorCode(request);
    return ResponseEntity.ok(ApiResponse.success("If the account exists, a new code has been sent", null));
}




@GetMapping("/me")
public ApiResponse<UserResponse> me() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    return ApiResponse.success("Current user retrieved", new UserResponse(user));
}

@PostMapping("/verify-device")
public ResponseEntity<ApiResponse<LoginResponse>> verifyDevice(@Valid @RequestBody VerifyDeviceRequest request,
                                                                 HttpServletRequest httpRequest,
                                                                 HttpServletResponse httpResponse) {
    LoginResponse response = authService.verifyDevice(request, httpRequest, httpResponse);
    return ResponseEntity.ok(ApiResponse.success("Device verification result", response));
}

@PostMapping("/verify-device/resend")
public ResponseEntity<ApiResponse<Void>> resendDeviceCode(@Valid @RequestBody ResendCodeRequest request) {
    authService.resendDeviceCode(request);
    return ResponseEntity.ok(ApiResponse.success("If the account exists, a new code has been sent", null));
}

@PostMapping("/forgot-password")
public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
    passwordResetService.requestReset(request);
    return ResponseEntity.ok(ApiResponse.success(
            "If an account with that email exists, a reset link has been sent", null));
}

@PostMapping("/reset-password")
public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request, HttpServletRequest httpRequest) {
    passwordResetService.resetPassword(request, httpRequest);
    return ApiResponse.success("Password reset successfully", null);
}



}