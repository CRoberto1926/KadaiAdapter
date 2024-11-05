package io.kadai.adapter.monitoring;

import jakarta.ws.rs.core.UriBuilder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class HealthCheck implements HealthIndicator {

  private final RestTemplate restTemplate;

  @Value("${camundaOutboxService.address}")
  private String camundaOutboxAddress;

  @Value("${camundaOutboxService.port}")
  private int camundaOutboxPort;

  @Value("${kadaiService.address}")
  private String kadaiAddress;

  @Value("${kadaiService.port}")
  private int kadaiPort;

  @Autowired
  public HealthCheck(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public Health health() {
    Health.Builder camundaHealth = Health.up();
    Health.Builder outboxHealth = Health.up();
    Health.Builder kadaiHealth = Health.up();
    try {
      ResponseEntity<EngineInfoRepresentationModel[]> camundaResponse = pingCamundaRest();
      EngineInfoRepresentationModel[] engines = camundaResponse.getBody();
      if (engines != null
          && engines.length > 0
          && camundaResponse.getStatusCode().equals(HttpStatus.OK)) {
        camundaHealth
            .withDetail("Camunda Engines", engines)
            .status(Health.up().build().getStatus());
      } else {
        camundaHealth
            .withDetail("Camunda Engine", "No engines found")
            .status(Health.down().build().getStatus());
      }
    } catch (Exception e) {
      camundaHealth
          .withDetail("Camunda Engine Error", e.getMessage())
          .status(Health.down().build().getStatus());
    }

    try {
      ResponseEntity<EventCountRepresentationModel> outboxResponse =
          pingOutBoxRestGetNumberFailedEvents();
      outboxHealth
          .withDetail("Outbox Service", outboxResponse.getBody())
          .status(Health.up().build().getStatus());
    } catch (Exception e) {
      outboxHealth
          .withDetail("Outbox Service Error", e.getMessage())
          .status(Health.down().build().getStatus());
    }

    try {
      ResponseEntity<VersionInfoRepresentationModel> kadaiResponse =
          pingKadaiGetCurrentSchemaVersion();
      kadaiHealth
          .withDetail("Kadai Service", kadaiResponse.getBody())
          .status(Health.up().build().getStatus());
    } catch (Exception e) {
      kadaiHealth
          .withDetail("Kadai Service Error", e.getMessage())
          .status(Health.down().build().getStatus());
    }

    return Health.status("External Services Status")
        .withDetail("Camunda Health", camundaHealth.build())
        .withDetail("Outbox Health", outboxHealth.build())
        .withDetail("Kadai Health", kadaiHealth.build())
        .build();
  }

  public static HttpHeaders generateHeadersForUser(String user) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", encodeUserAndPasswordAsBasicAuth(user));
    headers.add("Content-Type", "application/hal+json");
    return headers;
  }

  public static String encodeUserAndPasswordAsBasicAuth(String user) {
    String toEncode = user + ":" + user;
    return "Basic " + Base64.getEncoder().encodeToString(toEncode.getBytes(StandardCharsets.UTF_8));
  }

  private <T> ResponseEntity<T> pingService(String url, HttpEntity<?> auth, Class<T> responseType)
      throws Exception {
    try {
      return restTemplate.exchange(
          url, HttpMethod.GET, auth, ParameterizedTypeReference.forType(responseType));
    } catch (Exception e) {
      throw new Exception("Error connecting to service: " + e.getMessage(), e);
    }
  }

  private ResponseEntity<EngineInfoRepresentationModel[]> pingCamundaRest() throws Exception {
    String url =
        UriBuilder.fromUri(camundaOutboxAddress)
            .port(camundaOutboxPort)
            .path("example-context-root/engine-rest/engine")
            .build()
            .toString();
    return pingService(url, null, EngineInfoRepresentationModel[].class);
  }

  private ResponseEntity<EventCountRepresentationModel> pingOutBoxRestGetNumberFailedEvents()
      throws Exception {
    String url =
        UriBuilder.fromUri(camundaOutboxAddress)
            .port(camundaOutboxPort)
            .path("example-context-root/outbox-rest/events/count")
            .queryParam("retries", 0)
            .build()
            .toString();
    return pingService(url, null, EventCountRepresentationModel.class);
  }

  private ResponseEntity<VersionInfoRepresentationModel> pingKadaiGetCurrentSchemaVersion()
      throws Exception {
    String url =
        UriBuilder.fromUri(kadaiAddress)
            .port(kadaiPort)
            .path("kadai/api/v1/version")
            .build()
            .toString();
    HttpEntity<?> auth = new HttpEntity<>(generateHeadersForUser("admin"));

    return pingService(url, auth, VersionInfoRepresentationModel.class);
  }

  public static class EngineInfoRepresentationModel {

    private String name;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  public static class EventCountRepresentationModel {

    private int eventsCount;

    public int getEventsCount() {
      return eventsCount;
    }

    public void setEventsCount(int eventsCount) {
      this.eventsCount = eventsCount;
    }
  }

  public static class VersionInfoRepresentationModel {

    private String version;

    public String getVersion() {
      return version;
    }

    public void setVersion(String version) {
      this.version = version;
    }
  }
}
