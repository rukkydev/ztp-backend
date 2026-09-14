package com.ztp.threat;

import com.ztp.common.ApiResponse;
import com.ztp.threat.dto.AlertResponse;
import com.ztp.threat.dto.UpdateStatusRequest;
import com.ztp.user.dto.BulkIdsRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService service;

    @GetMapping
    @PreAuthorize("hasAuthority('THREAT_MANAGE')")
    public ApiResponse<List<AlertResponse>> list() {
        return ApiResponse.success("Alerts retrieved", service.listAll());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('THREAT_MANAGE')")
    public ApiResponse<AlertResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
        return ApiResponse.success("Alert updated", service.updateStatus(id, request.getStatus()));
    }

    @PatchMapping("/bulk-resolve")
    @PreAuthorize("hasAuthority('THREAT_MANAGE')")
    public ApiResponse<List<AlertResponse>> bulkResolve(@Valid @RequestBody BulkIdsRequest request) {
        return ApiResponse.success("Alerts resolved", service.bulkResolve(request.getIds()));
    }
}