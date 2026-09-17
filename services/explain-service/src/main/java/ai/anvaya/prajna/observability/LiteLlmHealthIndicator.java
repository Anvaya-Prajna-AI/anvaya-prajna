package ai.anvaya.prajna.observability;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component("liteLlmHealthIndicator")
public class LiteLlmHealthIndicator implements HealthIndicator {

    private final String baseUrl;
    private final HttpClient httpClient;

    public LiteLlmHealthIndicator(
            @Value("${spring.ai.openai.base-url:http://localhost:4000}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(800))
                .build();
    }

    @Override
    public Health health() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/health"))
                    .timeout(Duration.ofMillis(1200))
                    .GET()
                    .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() >= 200 && response.statusCode() < 400) {
                return Health.up()
                        .withDetail("gateway", "LiteLLM / AI Proxy")
                        .withDetail("endpoint", baseUrl)
                        .withDetail("status", response.statusCode())
                        .build();
            } else {
                return Health.down()
                        .withDetail("gateway", "LiteLLM / AI Proxy")
                        .withDetail("endpoint", baseUrl)
                        .withDetail("statusCode", response.statusCode())
                        .build();
            }
        } catch (Exception e) {
            // Degraded or down, report with detail without breaking application startup
            return Health.unknown()
                    .withDetail("gateway", "LiteLLM / AI Proxy")
                    .withDetail("endpoint", baseUrl)
                    .withDetail("message", e.getMessage() != null ? e.getMessage() : "Gateway not reachable")
                    .build();
        }
    }
}
