package com.ztp.account;

import com.ztp.account.dto.ProfileResponse;
import com.ztp.account.dto.UpdateProfileRequest;
import com.ztp.user.User;
import com.ztp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.ztp.audit.AuditLogService;
import com.ztp.account.dto.ToggleTwoFactorRequest;
import jakarta.servlet.http.HttpServletRequest;


@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuditLogService auditLogService;
	private final com.ztp.mail.EmailTemplateService emailTemplateService;

    public ProfileResponse getMyProfile() {
        return new ProfileResponse(currentUser());
    }

    public ProfileResponse updateMyProfile(UpdateProfileRequest request) {
        User user = currentUser();

        if (!user.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        user.setUsername(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setJobTitle(request.getJobTitle());
        user.setDepartment(request.getDepartment());

        return new ProfileResponse(userRepository.save(user));
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }
		
		
	public void toggleTwoFactor(ToggleTwoFactorRequest request, HttpServletRequest httpRequest) {
		User user = currentUser();

		if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
			auditLogService.record("TWO_FACTOR_DISABLE_FAILED", user.getId(), user.getUsername(),
					"Incorrect password provided for 2FA change", httpRequest);
			throw new IllegalArgumentException("Incorrect password");
		}

		user.setTwoFactorEnabled(request.isEnabled());
		userRepository.save(user);

		String eventType = request.isEnabled() ? "TWO_FACTOR_ENABLED" : "TWO_FACTOR_DISABLED";
		auditLogService.record(eventType, user.getId(), user.getUsername(),
				"Two-factor authentication " + (request.isEnabled() ? "enabled" : "disabled") + " by user",
				httpRequest);

		if (!request.isEnabled()) {
			sendTwoFactorDisabledEmail(user);
		}
	}

	private void sendTwoFactorDisabledEmail(User user) {
		emailTemplateService.sendTwoFactorDisabledEmail(user.getEmail(), user.getUsername());
	}
}