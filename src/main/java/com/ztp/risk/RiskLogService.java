package com.ztp.risk;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RiskLogService {

    private final RiskEvaluationLogRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    public void record(String correlationId, RiskRequest request, RiskResponse response,
                    String enforcedAction, boolean evaluateReached) {
    RiskEvaluationLog log = new RiskEvaluationLog();
    log.setCorrelationId(correlationId);
    log.setUserId(request.getUserId());
    log.setEventType(request.getEventType());
    log.setNewDevice(request.isNewDevice());
    log.setNewIp(request.isNewIp());
    log.setLoginHourUnusual(request.isLoginHourUnusual());
    log.setFailedLoginsLast10Min(request.getFailedLoginsLast10Min());
    log.setTwoFactorFailuresLast10Min(request.getTwoFactorFailuresLast10Min());
    log.setDeviceId(request.getDeviceId());
    log.setBrowser(request.getBrowser());
    log.setOperatingSystem(request.getOperatingSystem());
    log.setIpAddress(request.getIpAddress());
    log.setRiskScore(response.getRiskScore());
    log.setRiskLevel(response.getRiskLevel());
    log.setRecommendedAction(response.getRecommendedAction());
    log.setEnforcedAction(enforcedAction);
    log.setEvaluateReached(evaluateReached);

    try {
        log.setReasonsJson(objectMapper.writeValueAsString(response.getReasons()));
    } catch (Exception ignored) {
        log.setReasonsJson("[]");
    }

    log.setEngineVersion(response.getEngineVersion());

    repository.save(log);
}

    public String exportAsJsonLines(LocalDateTime from, LocalDateTime to) {
        List<RiskEvaluationLog> logs = repository.findAllByEvaluatedAtBetweenOrderByEvaluatedAtAsc(from, to);
        StringBuilder sb = new StringBuilder();
        for (RiskEvaluationLog log : logs) {
            try {
                sb.append(objectMapper.writeValueAsString(log)).append("\n");
            } catch (Exception ignored) { }
        }
        return sb.toString();
    }
}