package com.ztp.risk;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class RiskEvaluationResult {
    private final RiskResponse response;
    private final boolean evaluateReached;
}