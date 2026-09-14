package com.ztp.device;

import com.ztp.device.dto.DeviceResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import com.ztp.audit.AuditLogService;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
		private final com.ztp.session.device.ActiveSessionDeviceRepository activeSessionDeviceRepository;
private final org.springframework.security.core.session.SessionRegistry sessionRegistry;
private final AuditLogService auditLogService;

    public boolean isTrustedDevice(Long userId, String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            return false;
        }
        return deviceRepository.findByUserIdAndDeviceId(userId, deviceId)
                .map(Device::isTrusted)
                .orElse(false);
    }

    public boolean isBlocked(Long userId, String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            return false;
        }
        return deviceRepository.findByUserIdAndDeviceId(userId, deviceId)
                .map(Device::isBlocked)
                .orElse(false);
    }

    public void recordSighting(Long userId, String deviceId, HttpServletRequest httpRequest, boolean markTrusted) {
        if (deviceId == null || deviceId.isBlank()) {
            return;
        }

        Device device = deviceRepository.findByUserIdAndDeviceId(userId, deviceId)
                .orElseGet(Device::new);

        device.setUserId(userId);
        device.setDeviceId(deviceId);
        device.setUserAgent(httpRequest.getHeader("User-Agent"));
        device.setIpAddress(resolveIp(httpRequest));
        device.setLastSeenAt(LocalDateTime.now());
        if (markTrusted) {
            device.setTrusted(true);
        }

        deviceRepository.save(device);
    }

    public List<DeviceResponse> listAllDevices() {
        return deviceRepository.findAll().stream()
                .map(DeviceResponse::new)
                .toList();
    }





public DeviceResponse updateDeviceStatus(Long deviceId, String status) {
    Device device = deviceRepository.findById(deviceId)
            .orElseThrow(() -> new IllegalArgumentException("Device not found"));

    switch (status) {
        case "Blocked" -> {
            device.setBlocked(true);
            device.setTrusted(false);
            terminateSessionsForDevice(device.getUserId(), device.getDeviceId());
        }
        case "Trusted" -> { device.setBlocked(false); device.setTrusted(true); }
        default -> throw new IllegalArgumentException("Invalid status: must be 'Blocked' or 'Trusted'");
    }

    return new DeviceResponse(deviceRepository.save(device));
}

private void terminateSessionsForDevice(Long userId, String deviceId) {
    var mappings = activeSessionDeviceRepository.findAllByUserIdAndDeviceId(userId, deviceId);
    for (var mapping : mappings) {
        var info = sessionRegistry.getSessionInformation(mapping.getSessionId());
        if (info != null) {
            info.expireNow();
        }
    }
    if (!mappings.isEmpty()) {
        auditLogService.record("SESSION_TERMINATED", null, "system",
                "Session(s) terminated due to device block", null);
    }
}



	public List<DeviceResponse> listMyDevices(Long userId) {
		return deviceRepository.findAllByUserId(userId).stream()
				.map(DeviceResponse::new)
					.toList();
	}

	public void removeMyDevice(Long userId, Long deviceId, String currentDeviceId) {
		Device device = deviceRepository.findById(deviceId)
				.filter(d -> d.getUserId().equals(userId)) // ownership check -- 404, not 403, to avoid leaking existence
				.orElseThrow(() -> new IllegalArgumentException("Device not found"));

		if (device.getDeviceId().equals(currentDeviceId)) {
			throw new IllegalArgumentException("Cannot remove the device you're currently using");
		}

		deviceRepository.delete(device);
	}
	
	public List<DeviceResponse> bulkBlock(List<Long> ids) {
    List<Device> devices = deviceRepository.findAllById(ids);

    if (devices.size() != ids.size()) {
        throw new IllegalArgumentException("One or more device IDs are invalid");
    }

    devices.forEach(device -> {
        device.setBlocked(true);
        device.setTrusted(false);
    });

    return deviceRepository.saveAll(devices).stream()
            .map(DeviceResponse::new)
            .toList();
}

    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}