package com.ztp.account;

import com.ztp.account.dto.ProfileResponse;
import com.ztp.account.dto.UpdateProfileRequest;
import com.ztp.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.ztp.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import com.ztp.account.dto.ToggleTwoFactorRequest;


@RestController
@RequestMapping("/api/account/profile")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
	private final UserRepository userRepository;
	private final AvatarService avatarService;
	private final com.ztp.device.DeviceService deviceService;
	private final com.ztp.session.SessionService sessionService;
	private final com.ztp.notification.inapp.NotificationService notificationService;
	private final com.ztp.account.recovery.RecoveryPhraseService recoveryPhraseService;
	
    @GetMapping
    public ApiResponse<ProfileResponse> getProfile() {
        return ApiResponse.success("Profile retrieved", accountService.getMyProfile());
    }

    @PatchMapping
    public ApiResponse<ProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.success("Profile updated", accountService.updateMyProfile(request));
    }
	
	@PostMapping("/avatar")
	public ApiResponse<String> uploadAvatar(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
		Long userId = currentUserId();
		String url = avatarService.uploadAvatar(userId, file);
		return ApiResponse.success("Avatar uploaded", url);
	}

	private Long currentUserId() {
		String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalStateException("Authenticated user not found"))
				.getId();
	}
	
	@GetMapping("/overview")
	public ApiResponse<com.ztp.account.dto.AccountOverviewResponse> overview() {
		Long userId = currentUserId();
		String email = org.springframework.security.core.context.SecurityContextHolder
				.getContext().getAuthentication().getName();

		ProfileResponse profile = accountService.getMyProfile();
		int deviceCount = deviceService.listMyDevices(userId).size();
		int sessionCount = sessionService.listMySessions(email).size();
		long unread = notificationService.unreadCount(userId);
		boolean hasPhrase = recoveryPhraseService.hasRecoveryPhrase(userId);

		var response = new com.ztp.account.dto.AccountOverviewResponse(profile, deviceCount, sessionCount, unread, hasPhrase);
		return ApiResponse.success("Account overview retrieved", response);
	}

	@PatchMapping("/two-factor")
		public ApiResponse<Void> toggleTwoFactor(@Valid @RequestBody ToggleTwoFactorRequest request,
												  HttpServletRequest httpRequest) {
			accountService.toggleTwoFactor(request, httpRequest);
			return ApiResponse.success(
					request.isEnabled() ? "Two-factor authentication enabled" : "Two-factor authentication disabled",
					null);
	}


}