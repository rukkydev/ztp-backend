package com.ztp.threat;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ResponseActionService {

    private static final Logger log = LoggerFactory.getLogger(ResponseActionService.class);
    private final ResponseActionRepository repository;

    public ResponseAction record(String correlationId, String actionType, String status,
                                 String details, Long userId, String targetEntity) {
        ResponseAction action = ResponseAction.builder()
                .correlationId(correlationId)
                .actionType(actionType)
                .status(status)
                .details(details)
                .userId(userId)
                .targetEntity(targetEntity)
                .build();
        ResponseAction saved = repository.save(action);
        log.info("Recorded response action: [type={}, status={}, correlationId={}]", actionType, status, correlationId);
        return saved;
    }
}
