package io.kadai.adapter.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import io.kadai.adapter.models.CamundaEngineInfoRepresentationModel;
import io.kadai.adapter.monitoring.CamundaHealthCheck;
import io.kadai.adapter.test.KadaiAdapterTestApplication;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.common.test.security.WithAccessId;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(
    classes = KadaiAdapterTestApplication.class,
    webEnvironment = WebEnvironment.DEFINED_PORT)
@AutoConfigureWebTestClient
@ExtendWith(JaasExtension.class)
@ContextConfiguration
class CamundaHealthCheckTest extends AbsIntegrationTest {

  private MockRestServiceServer mockServer;

  @Autowired private RestTemplate restTemplate;

  @Autowired private CamundaHealthCheck camundaHealthIndicator;

  @BeforeEach
  @WithAccessId(user = "admin")
  void setUp() throws Exception {
    super.setUp();
    mockServer = MockRestServiceServer.createServer(restTemplate);
  }

  @AfterEach
  @WithAccessId(user = "admin")
  void reset() {
    mockServer.reset();
  }

  @Test
  void should_ReturnUp_When_CamundaServiceIsHealthy() {
    mockServer
        .expect(requestTo("http://localhost:8081/example-context-root/engine-rest/engine"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("[{\"name\": \"default\"}]", MediaType.APPLICATION_JSON));
    dummyOutboxMock();

    ResponseEntity<String> response =
        testRestTemplate.getForEntity(
            "http://localhost:10020/actuator/health/external-services", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
        .contains("\"Camunda Health\":{\"status\":\"UP\"")
        .contains("\"Camunda Engines\":[{\"name\":\"default\"}]");
  }

  @Test
  void should_ReturnDown_When_CheckingCamundaEngineHealth() {
    mockServer
        .expect(requestTo("http://localhost:8081/example-context-root/engine-rest/engine"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
    dummyOutboxMock();

    ResponseEntity<String> response =
        testRestTemplate.getForEntity(
            "http://localhost:10020/actuator/health/external-services", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
        .contains("\"Camunda Health\":{\"status\":\"DOWN\"")
        .contains("\"Camunda Engine\":\"No engines found\"");
  }

  @Test
  void should_ThrowException_When_CheckingCamundaEngineHealth() {
    mockServer
        .expect(requestTo("http://localhost:8081/example-context-root/engine-rest/engine"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.NOT_FOUND)
            .body("Page Not Found")
            .contentType(MediaType.TEXT_PLAIN));
    dummyOutboxMock();

    ResponseEntity<String> response =
        testRestTemplate.getForEntity(
            "http://localhost:10020/actuator/health/external-services", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
        .contains("\"Camunda Health\":{\"status\":\"DOWN\"")
        .contains(
            "\"Camunda Engine Error\":\"Error connecting to service: " + "404 Page Not Found\"");
  }

  private void dummyOutboxMock() {
    this.mockServer
        .expect(
            requestTo(
                "http://localhost:8081/example-context-root/outbox-rest/events/count?retries=0"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("{\"eventsCount\": 5}", MediaType.APPLICATION_JSON));
  }
}
