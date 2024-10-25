package io.kadai.adapter.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.kadai.adapter.monitoring.HealthCheck.EngineInfoRepresentationModel;
import io.kadai.adapter.monitoring.HealthCheck.EventCountRepresentationModel;
import io.kadai.adapter.monitoring.HealthCheck.VersionInfoRepresentationModel;
import io.kadai.adapter.test.KadaiAdapterTestApplication;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.common.test.security.WithAccessId;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(
    classes = KadaiAdapterTestApplication.class,
    webEnvironment = WebEnvironment.DEFINED_PORT)
@ExtendWith(JaasExtension.class)
@ContextConfiguration
class HealthCheckTest extends AbsIntegrationTest {

  @MockBean private RestTemplate restTemplate;
  @LocalServerPort private Integer port;

  @WithAccessId(user = "admin")
  @Test
  void should_ReturnUp_When_CheckingCamundaEngineHealth() {
    EngineInfoRepresentationModel[] engines = new EngineInfoRepresentationModel[1];
    engines[0] = new EngineInfoRepresentationModel();
    engines[0].setName("default");

    ParameterizedTypeReference<EngineInfoRepresentationModel[]> responseType =
        new ParameterizedTypeReference<>() {};

    Mockito.when(
            restTemplate.exchange(
                Mockito.eq("http://localhost:8081/example-context-root/engine-rest/engine"),
                Mockito.eq(HttpMethod.GET),
                Mockito.any(),
                Mockito.eq(responseType)))
        .thenReturn(ResponseEntity.ok(engines));

    TestRestTemplate testRestTemplate = createRestTemplate();
    ResponseEntity<String> response =
        testRestTemplate.getForEntity("/actuator/health/external-services", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
        .contains("\"Camunda Health\":{\"status\":\"UP\"")
        .contains("\"Camunda Engines\":[{\"name\":\"default\"}]");
  }

  @WithAccessId(user = "admin")
  @Test
  void should_ReturnDown_When_CheckingCamundaEngineHealth() {
    EngineInfoRepresentationModel[] engines = new EngineInfoRepresentationModel[0];

    ParameterizedTypeReference<EngineInfoRepresentationModel[]> responseType =
        new ParameterizedTypeReference<>() {};

    Mockito.when(
            restTemplate.exchange(
                Mockito.eq("http://localhost:8081/example-context-root/engine-rest/engine"),
                Mockito.eq(HttpMethod.GET),
                Mockito.any(),
                Mockito.eq(responseType)))
        .thenReturn(ResponseEntity.ok(engines));

    TestRestTemplate testRestTemplate = createRestTemplate();
    ResponseEntity<String> response =
        testRestTemplate.getForEntity("/actuator/health/external-services", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
        .contains("\"Camunda Health\":{\"status\":\"DOWN\"")
        .contains("\"Camunda Engine\":\"No engines found\"");
  }

  @WithAccessId(user = "admin")
  @Test
  void should_ThrowException_When_CheckingCamundaEngineHealth() {
    ParameterizedTypeReference<EngineInfoRepresentationModel[]> responseType =
        new ParameterizedTypeReference<>() {};

    Mockito.when(
            restTemplate.exchange(
                Mockito.eq("http://localhost:8081/example-context-root/engine-rest/engine"),
                Mockito.eq(HttpMethod.GET),
                Mockito.any(),
                Mockito.eq(responseType)))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Page Not Found"));

    TestRestTemplate testRestTemplate = createRestTemplate();
    ResponseEntity<String> response =
        testRestTemplate.getForEntity("/actuator/health/external-services", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
        .contains("\"Camunda Health\":{\"status\":\"DOWN\"")
        .contains(
            "\"Camunda Engine Error\":\"Error connecting to service: "
                + "404 Page Not Found\"");
  }

  @WithAccessId(user = "admin")
  @Test
  void should_ReturnUp_When_CheckingOutboxHealth() {
    EventCountRepresentationModel eventCount = new EventCountRepresentationModel();
    eventCount.setEventsCount(1);

    ParameterizedTypeReference<EventCountRepresentationModel> responseType =
        new ParameterizedTypeReference<>() {};

    Mockito.when(
            restTemplate.exchange(
                Mockito.eq(
                    "http://localhost:8081/example-context-root/outbox-rest/events/count?retries=0"),
                Mockito.eq(HttpMethod.GET),
                Mockito.any(),
                Mockito.eq(responseType)))
        .thenReturn(ResponseEntity.ok(eventCount));

    TestRestTemplate testRestTemplate = createRestTemplate();
    ResponseEntity<String> response =
        testRestTemplate.getForEntity("/actuator/health/external-services", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
        .contains("\"Outbox Health\":{\"status\":\"UP\"")
        .contains("\"Outbox Service\":{\"eventsCount\":1}");
  }

  @WithAccessId(user = "admin")
  @Test
  void should_ThrowException_When_CheckingOutboxHealth() {
    ParameterizedTypeReference<EventCountRepresentationModel> responseType =
        new ParameterizedTypeReference<>() {};

    Mockito.when(
            restTemplate.exchange(
                Mockito.eq(
                    "http://localhost:8081/example-context-root/outbox-rest/events/count?retries=0"),
                Mockito.eq(HttpMethod.GET),
                Mockito.any(),
                Mockito.eq(responseType)))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Page Not Found"));

    TestRestTemplate testRestTemplate = createRestTemplate();
    ResponseEntity<String> response =
        testRestTemplate.getForEntity("/actuator/health/external-services", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
        .contains("\"Outbox Health\":{\"status\":\"DOWN\"")
        .contains(
            "\"Outbox Service Error\":\"Error connecting to service: "
                + "404 Page Not Found\"");
  }

  @WithAccessId(user = "admin")
  @Test
  void should_ReturnUp_When_CheckingKadaiHealth() {
    VersionInfoRepresentationModel versionInfo = new VersionInfoRepresentationModel();
    versionInfo.setVersion("1.0.0");

    ParameterizedTypeReference<VersionInfoRepresentationModel> responseType =
        new ParameterizedTypeReference<>() {};

    String user = "admin";

    Mockito.when(
            restTemplate.exchange(
                Mockito.eq("http://localhost:8080/kadai/api/v1/version"),
                Mockito.eq(HttpMethod.GET),
                Mockito.argThat(
                    httpEntity -> {
                      HttpHeaders headers = httpEntity.getHeaders();
                      boolean hasAuthorization =
                          headers.containsKey("Authorization")
                              && headers
                                  .get("Authorization")
                                  .contains(
                                      "Basic "
                                          + Base64.getEncoder()
                                              .encodeToString(
                                                  (user + ":" + user)
                                                      .getBytes(StandardCharsets.UTF_8)));
                      boolean hasContentType =
                          headers.containsKey("Content-Type")
                              && headers.get("Content-Type").contains("application/hal+json");

                      return hasAuthorization && hasContentType;
                    }),
                Mockito.eq(responseType)))
        .thenReturn(ResponseEntity.ok(versionInfo));

    TestRestTemplate testRestTemplate = createRestTemplate();
    ResponseEntity<String> response =
        testRestTemplate.getForEntity("/actuator/health/external-services", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
        .contains("\"Kadai Health\":{\"status\":\"UP\"")
        .contains("\"Kadai Service\":{\"version\":\"1.0.0\"}");
  }

  @WithAccessId(user = "admin")
  @Test
  void should_ThrowException_When_CheckingKadaiHealth() {
    ParameterizedTypeReference<VersionInfoRepresentationModel> responseType =
        new ParameterizedTypeReference<>() {};

    String user = "admin";

    Mockito.when(
            restTemplate.exchange(
                Mockito.eq("http://localhost:8080/kadai/api/v1/version"),
                Mockito.eq(HttpMethod.GET),
                Mockito.argThat(
                    httpEntity -> {
                      HttpHeaders headers = httpEntity.getHeaders();
                      boolean hasAuthorization =
                          headers.containsKey("Authorization")
                              && headers
                                  .get("Authorization")
                                  .contains(
                                      "Basic "
                                          + Base64.getEncoder()
                                              .encodeToString(
                                                  (user + ":" + user)
                                                      .getBytes(StandardCharsets.UTF_8)));
                      boolean hasContentType =
                          headers.containsKey("Content-Type")
                              && headers.get("Content-Type").contains("application/hal+json");

                      return hasAuthorization && hasContentType;
                    }),
                Mockito.eq(responseType)))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Page Not Found"));

    TestRestTemplate testRestTemplate = createRestTemplate();
    ResponseEntity<String> response =
        testRestTemplate.getForEntity("/actuator/health/external-services", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
        .contains("\"Kadai Health\":{\"status\":\"DOWN\"")
        .contains(
            "\"Kadai Service Error\":\"Error connecting to service: "
                + "404 Page Not Found\"");
  }

  private TestRestTemplate createRestTemplate() {
    return new TestRestTemplate(
        new RestTemplateBuilder()
            .rootUri("http://localhost:" + port)
            .requestFactory(HttpComponentsClientHttpRequestFactory.class));
  }
}
