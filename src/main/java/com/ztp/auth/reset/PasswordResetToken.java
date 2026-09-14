package com.ztp.auth.reset;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // SHA-256 hex digest -- deterministic and indexable, unlike BCrypt,
    // which is intentionally non-deterministic. Appropriate here because
    // the token itself carries ~256 bits of entropy (unguessable), so we
    // don't need BCrypt's slow-hashing brute-force protection.
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

	@Builder.Default
	@Column(nullable = false)
	private boolean consumed = false;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}