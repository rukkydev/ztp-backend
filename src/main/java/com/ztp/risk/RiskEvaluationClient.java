package com.ztp.risk;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RiskEvaluationClient {

    @Value("${app.ml-service-url}")
    private String mlServiceUrl;

    public RiskResponse evaluate(RiskRequest request) {
        try {
            RestClient client = RestClient.builder()
                    .baseUrl(mlServiceUrl)
                    .requestFactory(clientRequestFactory())
                    .build();

            RiskResponse response = client.post()
                    .uri("/risk/evaluate")
                    .body(request)
                    .retrieve()
                    .body(RiskResponse.class);

            return response != null ? response : safeDefault();
        } catch (RuntimeException e) {
            // ML service unreachable, slow, or erroring -- never let this
            // block a login. Fail open to ALLOW, per the agreed contract.
            return safeDefault();
        }
    }
	
	
	public RiskEvaluationResult evaluateWithMeta(RiskRequest request) {
    try {
        RestClient client = RestClient.builder()
                .baseUrl(mlServiceUrl)
                .requestFactory(clientRequestFactory())
                .build();

        RiskResponse response = client.post()
                .uri("/risk/evaluate")
                .body(request)
                .retrieve()
                .body(RiskResponse.class);

        return new RiskEvaluationResult(response != null ? response : safeDefault(), true);
    } catch (RuntimeException e) {
        return new RiskEvaluationResult(safeDefault(), false);
    }
}
	

    private org.springframework.http.client.SimpleClientHttpRequestFactory clientRequestFactory() {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofMillis(400).toMillis());
        factory.setReadTimeout((int) Duration.ofMillis(400).toMillis());
        return factory;
    }
	
	private RiskResponse safeDefault() {
    return new RiskResponse(0, "LOW", "ALLOW", java.util.List.of(), null);
}
}