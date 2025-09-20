package com.reliaquest.api.client;

import com.reliaquest.api.model.ApiEnvelope;
import com.reliaquest.api.model.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import org.springframework.http.HttpMethod;
import java.util.Map;

@Component
public class MockEmployeeApiClient {
    private static final Logger log = LoggerFactory.getLogger(MockEmployeeApiClient.class);
    private final WebClient webClient;

    private static final ParameterizedTypeReference<ApiEnvelope<Employee>> EMP_ENV =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<ApiEnvelope<List<Employee>>> EMP_LIST_ENV =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<ApiEnvelope<Boolean>> BOOL_ENV =
            new ParameterizedTypeReference<>() {};

    public MockEmployeeApiClient(WebClient employeeWebClient) {
        this.webClient = employeeWebClient;
    }

    private static Retry retryPolicy() {
    return Retry.fixedDelay(6, Duration.ofMillis(500)) // 6 tries, 0.5s delay
            .jitter(0.5)
            .filter(err -> {
                if (err instanceof WebClientResponseException w) {
                    int s = w.getStatusCode().value();
                    return s == 429 || (s >= 500 && s < 600);
                }
                return false;
            })
            .doBeforeRetry(rs -> {
                Throwable t = rs.failure();
                String msg = (t == null) ? "unknown" : t.toString();
                long attempt = rs.totalRetries() + 1;
                log.warn("Retrying transient error (attempt {}/{}): {}", attempt, 6, msg);
            });
    }

    public Mono<List<Employee>> fetchAll() {
        return webClient.get()
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
                          r -> r.createException().flatMap(Mono::error))
                .bodyToMono(EMP_LIST_ENV)
                .map(ApiEnvelope::data)
                .retryWhen(retryPolicy());
    }

    public Mono<Employee> fetchById(String id) {
        return webClient.get()
                .uri("/{id}", id)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
                          r -> r.createException().flatMap(Mono::error))
                .bodyToMono(EMP_ENV)
                .map(ApiEnvelope::data)
                .retryWhen(retryPolicy());
    }

    public Mono<Employee> create(CreateEmployeeRequest input) {
        return webClient.post()
                .bodyValue(input)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
                          r -> r.createException().flatMap(Mono::error))
                .bodyToMono(EMP_ENV)
                .map(ApiEnvelope::data)
                .retryWhen(retryPolicy());
    }

    public Mono<Boolean> deleteByName(String name) {
    return webClient.method(HttpMethod.DELETE)
            .uri("/{name}", name)
            .bodyValue(Map.of("name", name)) // <— required by mock server
            .retrieve()
            .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
                      r -> r.createException().flatMap(Mono::error))
            .bodyToMono(BOOL_ENV)
            .map(ApiEnvelope::data)
            .retryWhen(retryPolicy());
    }
}
