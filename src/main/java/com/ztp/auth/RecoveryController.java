package com.ztp.auth;

import com.ztp.account.recovery.RecoveryPhraseService;
import com.ztp.account.recovery.dto.RecoverAccountRequest;
import com.ztp.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RecoveryController {

    private final RecoveryPhraseService recoveryPhraseService;

    @PostMapping("/recover-with-phrase")
    public ApiResponse<Void> recover(@Valid @RequestBody RecoverAccountRequest request, HttpServletRequest httpRequest) {
        recoveryPhraseService.recoverAccount(request.getEmail(), request.getPhrase(), request.getNewPassword(), httpRequest);
        return ApiResponse.success("Account recovered successfully", null);
    }
}