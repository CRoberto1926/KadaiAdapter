package io.kadai.adapter.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import io.kadai.adapter.monitoring.HealthCheck.EngineInfoRepresentationModel;
import io.kadai.adapter.monitoring.HealthCheck.EventCountRepresentationModel;
import io.kadai.adapter.monitoring.HealthCheck.VersionInfoRepresentationModel;
import io.kadai.adapter.test.KadaiAdapterTestApplication;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.common.test.security.WithAccessId;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(
    classes = KadaiAdapterTestApplication.class,
    webEnvironment = WebEnvironment.DEFINED_PORT)
@AutoConfigureWebTestClient
@ExtendWith(JaasExtension.class)
@ContextConfiguration
class HealthCheckTest extends AbsIntegrationTest {
  private MockRestServiceServer mockServer;
  @Autowired private RestTemplate restTemplate;

  @BeforeEach
  @WithAccessId(user = "admin")
  void setUp() throws Exception {
    super.setUp();
    mockServer = MockRestServiceServer.createServer(restTemplate);
  }

  @WithAccessId(user = "admin")
  @Test
  void should_ReturnUp_When_CheckingCamundaEngineHealth() {
    mockServer
        .expect(requestTo("http://localhost:8081/example-context-root/engine-rest/engine"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("[{\"name\": \"default\"}]", MediaType.APPLICATION_JSON));

    mockServer
        .expect(
            requestTo(
                "http://localhost:8081/example-context-root/outbox-rest/events/count?retries=0"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("[{\"name\": \"default\"}]", MediaType.APPLICATION_JSON));

    ResponseEntity<String> response =
        testRestTemplate.getForEntity(
            "http://localhost:10020/actuator/health/external-services", String.class);

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

    ResponseEntity<String> response =
        restTemplate.getForEntity("/actuator/health/external-services", String.class);

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

    ResponseEntity<String> response =
        restTemplate.getForEntity("/actuator/health/external-services", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
        .contains("\"Camunda Health\":{\"status\":\"DOWN\"")
        .contains(
            "\"Camunda Engine Error\":\"Error connecting to service: " + "404 Page Not Found\"");
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

    ResponseEntity<String> response =
        restTemplate.getForEntity("/actuator/health/external-services", String.class);

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

    ResponseEntity<String> response =
        restTemplate.getForEntity("/actuator/health/external-services", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
        .contains("\"Outbox Health\":{\"status\":\"DOWN\"")
        .contains(
            "\"Outbox Service Error\":\"Error connecting to service: " + "404 Page Not Found\"");
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

    ResponseEntity<String> response =
        restTemplate.getForEntity("/actuator/health/external-services", String.class);

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

    ResponseEntity<String> response =
        restTemplate.getForEntity("/actuator/health/external-services", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
        .contains("\"Kadai Health\":{\"status\":\"DOWN\"")
        .contains(
            "\"Kadai Service Error\":\"Error connecting to service: " + "404 Page Not Found\"");
  }
}
