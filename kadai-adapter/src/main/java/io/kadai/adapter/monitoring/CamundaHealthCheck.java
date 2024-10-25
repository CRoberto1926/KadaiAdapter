package io.kadai.adapter.monitoring;

import io.kadai.adapter.models.CamundaEngineInfoRepresentationModel;
import jakarta.ws.rs.core.UriBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CamundaHealthCheck implements HealthIndicator {

  private final RestTemplate restTemplate;

  @Value("${camundaOutboxService.address}")
  private String camundaOutboxAddress;

  @Value("${camundaOutboxService.port}")
  private int camundaOutboxPort;

  public CamundaHealthCheck(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public Health health() {
    try {
      ResponseEntity<CamundaEngineInfoRepresentationModel[]> response = pingCamundaRest();
      CamundaEngineInfoRepresentationModel[] engines = response.getBody();

      if(engines == null || engines.length == 0) {
        return Health.down()
            .withDetail("Camunda Engine Error", "No engines found")
            .build();
      }
      return Health.up()
          .withDetail("Camunda Engines", engines)
          .build();
    } catch (Exception e) {
      return Health.down().withDetail("Camunda Engine Error", e.getMessage()).build();
    }
  }

  private ResponseEntity<CamundaEngineInfoRepresentationModel[]> pingCamundaRest()
      throws Exception {
    String url =
        UriBuilder.fromUri(camundaOutboxAddress)
            .port(camundaOutboxPort)
            .path("example-context-root/engine-rest/engine")
            .build()
            .toString();
    return restTemplate.getForEntity(url, CamundaEngineInfoRepresentationModel[].class);
  }
}
