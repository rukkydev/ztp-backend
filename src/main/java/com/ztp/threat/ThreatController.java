package com.ztp.threat;

import com.ztp.common.ApiResponse;
import com.ztp.threat.dto.ThreatResponse;
import com.ztp.threat.dto.UpdateStatusRequest;
import com.ztp.user.dto.BulkIdsRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/threats")
@RequiredArgsConstructor
public class ThreatController {

    private final ThreatService service;

    @GetMapping
    @PreAuthorize("hasAuthority('THREAT_MANAGE')")
    public ApiResponse<List<ThreatResponse>> list() {
        return ApiResponse.success("Threats retrieved", service.listAll());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('THREAT_MANAGE')")
    public ApiResponse<ThreatResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
        return ApiResponse.success("Threat updated", service.updateStatus(id, request.getStatus()));
    }

    @PatchMapping("/bulk-mitigate")
    @PreAuthorize("hasAuthority('THREAT_MANAGE')")
    public ApiResponse<List<ThreatResponse>> bulkMitigate(@Valid @RequestBody BulkIdsRequest request) {
        return ApiResponse.success("Threats mitigated", service.bulkMitigate(request.getIds()));
    }
}