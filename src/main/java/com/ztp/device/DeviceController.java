package com.ztp.device;

import com.ztp.common.ApiResponse;
import com.ztp.device.dto.DeviceResponse;
import com.ztp.device.dto.UpdateDeviceStatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ApiResponse<List<DeviceResponse>> listDevices() {
        return ApiResponse.success("Devices retrieved", deviceService.listAllDevices());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse<DeviceResponse> updateStatus(@PathVariable Long id,
                                                      @Valid @RequestBody UpdateDeviceStatusRequest request) {
        return ApiResponse.success("Device status updated", deviceService.updateDeviceStatus(id, request.getStatus()));
    }
	
	@PatchMapping("/bulk-block")
	@PreAuthorize("hasAuthority('USER_MANAGE')")
	public ApiResponse<List<DeviceResponse>> bulkBlock(@Valid @RequestBody com.ztp.user.dto.BulkIdsRequest request) {
		return ApiResponse.success("Devices blocked", deviceService.bulkBlock(request.getIds()));
	}
}