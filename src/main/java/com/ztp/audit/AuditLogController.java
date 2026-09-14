package com.ztp.audit;

import com.ztp.audit.dto.AuditLogResponse;
import com.ztp.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping("/api/admin/activity-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
	@PreAuthorize("hasAuthority('AUDIT_VIEW')")
	public ApiResponse<com.ztp.common.PagedResponse<AuditLogResponse>> listLogs(
			@RequestParam(required = false) String status,
			@RequestParam(required = false, name = "timestamp_from") String from,
			@RequestParam(required = false, name = "timestamp_to") String to,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int pageSize) {

		return ApiResponse.success("Activity logs retrieved",
				auditLogService.search(status, from, to, page, pageSize));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('AUDIT_VIEW')")
	public ApiResponse<AuditLogResponse> getLog(@PathVariable Long id) {
		return ApiResponse.success("Activity log retrieved", auditLogService.getById(id));
	}
}