package com.ztp.risk;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RiskResponse {
    private int riskScore;
    private String riskLevel;
    private String recommendedAction;
    private List<ScoreReason> reasons;
    private String engineVersion;
}