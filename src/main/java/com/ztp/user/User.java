package com.ztp.user;

import com.ztp.role.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    @Column(name = "account_locked", nullable = false)
    private boolean accountLocked = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
	
	@Column(length = 30)
	private String phone;
	
	@Column(name = "avatar_url", length = 255)
	private String avatarUrl;

	@Column(name = "job_title", length = 100)
	private String jobTitle;

	@Column(length = 100)
	private String department;

	@Column(name = "last_login")
	private LocalDateTime lastLogin;
	
	@Column(name = "two_factor_enabled", nullable = false)
	private boolean twoFactorEnabled = true; // on by default for everyone

	@Column(name = "recovery_phrase_hash")
	private String recoveryPhraseHash;

	@Column(name = "recovery_phrase_set_at")
	private LocalDateTime recoveryPhraseSetAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}