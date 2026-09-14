package com.ztp.settings;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "security_toggles")
@Getter
@Setter
@NoArgsConstructor
public class SecurityToggle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "toggle_key", nullable = false, unique = true, length = 100)
    private String toggleKey;

    @Column(nullable = false, length = 150)
    private String label;

    @Column(nullable = false)
    private boolean enabled;
}