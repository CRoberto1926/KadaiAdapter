package io.kadai.adapter.monitoring;

import io.kadai.adapter.models.OutboxEventCountRepresentationModel;
import jakarta.ws.rs.core.UriBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OutboxHealthCheck implements HealthIndicator {

  private final RestTemplate restTemplate;

  @Value("${camundaOutboxService.address}")
  private String camundaOutboxAddress;

  @Value("${camundaOutboxService.port}")
  private int camundaOutboxPort;

  public OutboxHealthCheck(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public Health health() {
    try {
      ResponseEntity<OutboxEventCountRepresentationModel> response = pingOutBoxRest();
      return Health.up().withDetail("Outbox Service", response.getBody()).build();
    } catch (Exception e) {
      return Health.down().withDetail("Outbox Service Error", e.getMessage()).build();
    }
  }

  private ResponseEntity<OutboxEventCountRepresentationModel> pingOutBoxRest() throws Exception {
    String url =
        UriBuilder.fromUri(camundaOutboxAddress)
            .port(camundaOutboxPort)
            .path("example-context-root/outbox-rest/events/count")
            .queryParam("retries", 0)
            .build()
            .toString();
    return restTemplate.getForEntity(url, OutboxEventCountRepresentationModel.class);
  }
}
