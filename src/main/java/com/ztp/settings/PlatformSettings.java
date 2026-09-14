package com.ztp.settings;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "platform_settings")
@Getter
@Setter
@NoArgsConstructor
public class PlatformSettings {

    // Singleton row -- always id = 1.
    @Id
    private Long id = 1L;

    @Column(name = "org_name", length = 150)
    private String orgName;

    @Column(name = "support_email", length = 150)
    private String supportEmail;

    @Column(name = "session_timeout_minutes", nullable = false)
    private int sessionTimeoutMinutes = 30;
}