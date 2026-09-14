package com.ztp.user.dto;

import lombok.Getter;
import lombok.Setter;

// All fields optional -- only non-null ones get applied.
// Powers both the row-level "Suspend" action (status only)
// and the full "Edit" form (everything else).
@Getter
@Setter
public class UpdateUserRequest {
    private String status;      // "Active" | "Suspended"
    private String username;
    private String email;
    private String phone;
    private String jobTitle;
    private String department;
    private String role;        // role name, if reassigning
}