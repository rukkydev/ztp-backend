package com.ztp.user;

import com.ztp.common.ApiResponse;
import com.ztp.user.dto.AdminUserResponse;
import com.ztp.user.dto.BulkIdsRequest;
import com.ztp.user.dto.CreateUserRequest;
import com.ztp.user.dto.UpdateUserRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ApiResponse<?> listUsers(
            @RequestParam(required = false) Integer page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false, defaultValue = "id") String sort,
            @RequestParam(required = false, defaultValue = "desc") String dir,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status) {
        if (page != null) {
            return ApiResponse.success("Users retrieved", userService.searchUsers(page, pageSize, sort, dir, q, status));
        }
        return ApiResponse.success("Users retrieved", userService.listUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ApiResponse<AdminUserResponse> getUser(@PathVariable Long id) {
        return ApiResponse.success("User retrieved", userService.getUser(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<ApiResponse<AdminUserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        AdminUserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("User created", response));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse<AdminUserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.success("User updated", userService.updateUser(id, request));
    }

    @PatchMapping("/bulk-suspend")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse<List<AdminUserResponse>> bulkSuspend(@Valid @RequestBody BulkIdsRequest request) {
        return ApiResponse.success("Users suspended", userService.bulkSuspend(request.getIds()));
    }

    @PatchMapping("/{id}/unlock")
    @PreAuthorize("hasAuthority('USER_UNLOCK')")
    public ApiResponse<AdminUserResponse> unlock(@PathVariable Long id) {
        return ApiResponse.success("User unlocked", userService.unlockUser(id));
    }
}