package com.ztp.account.recovery;

import com.ztp.common.ApiResponse;
import com.ztp.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account/security/recovery-phrase")
@RequiredArgsConstructor
public class RecoveryPhraseController {

    private final RecoveryPhraseService service;
    private final UserRepository userRepository;

    @GetMapping("/status")
    public ApiResponse<Boolean> hasPhrase() {
        return ApiResponse.success("Recovery phrase status", service.hasRecoveryPhrase(currentUserId()));
    }

    @PostMapping("/generate")
    public ApiResponse<String> generate(HttpServletRequest httpRequest) {
        String phrase = service.generate(currentUserId(), httpRequest);
        return ApiResponse.success("Recovery phrase generated -- save it now, it will not be shown again", phrase);
    }

    private Long currentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"))
                .getId();
    }
}