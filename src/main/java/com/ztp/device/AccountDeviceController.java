package com.ztp.device;

import com.ztp.common.ApiResponse;
import com.ztp.device.dto.DeviceResponse;
import com.ztp.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account/devices")
@RequiredArgsConstructor
public class AccountDeviceController {

    private final DeviceService deviceService;
    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<List<DeviceResponse>> myDevices() {
        return ApiResponse.success("Devices retrieved", deviceService.listMyDevices(currentUserId()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> removeDevice(@PathVariable Long id, HttpServletRequest httpRequest) {
        String currentDeviceId = httpRequest.getHeader("X-Device-Id");
        deviceService.removeMyDevice(currentUserId(), id, currentDeviceId);
        return ApiResponse.success("Device removed", null);
    }

    private Long currentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"))
                .getId();
    }
}