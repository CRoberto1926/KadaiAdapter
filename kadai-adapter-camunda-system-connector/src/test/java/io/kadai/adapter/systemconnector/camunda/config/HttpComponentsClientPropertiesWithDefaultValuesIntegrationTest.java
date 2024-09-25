/*
 * Copyright [2024] [envite consulting GmbH]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *
 */

package io.kadai.adapter.systemconnector.camunda.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.kadai.adapter.systemconnector.camunda.config.HttpComponentsClientPropertiesWithDefaultValuesIntegrationTest.OkHttpPropertiesWithDefaultValuesIntegrationTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {OkHttpPropertiesWithDefaultValuesIntegrationTestConfiguration.class})
class HttpComponentsClientPropertiesWithDefaultValuesIntegrationTest {

  @Test
  void should_HaveDefaultConnectionTimeout2000ms_When_NoPropertyOkHttpConnectionTimeoutIsSet(
      @Autowired HttpComponentsClientProperties httpComponentsClientProperties) {
    assertThat(httpComponentsClientProperties.getConnectionTimeout()).isEqualTo(2_000);
  }

  @Test
  void should_HaveDefaultReadTimeout5000ms_When_NoPropertyOkHttpReadTimeoutIsSet(
      @Autowired HttpComponentsClientProperties httpComponentsClientProperties) {
    assertThat(httpComponentsClientProperties.getReadTimeout()).isEqualTo(5_000);
  }

  @EnableConfigurationProperties(HttpComponentsClientProperties.class)
  static class OkHttpPropertiesWithDefaultValuesIntegrationTestConfiguration {}
}
