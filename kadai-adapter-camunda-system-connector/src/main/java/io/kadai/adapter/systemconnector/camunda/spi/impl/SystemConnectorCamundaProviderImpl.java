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

package io.kadai.adapter.systemconnector.camunda.spi.impl;

import io.kadai.adapter.configuration.AdapterSpringContextProvider;
import io.kadai.adapter.systemconnector.api.SystemConnector;
import io.kadai.adapter.systemconnector.camunda.api.impl.CamundaSystemConnectorImpl;
import io.kadai.adapter.systemconnector.camunda.config.CamundaSystemUrls;
import io.kadai.adapter.systemconnector.spi.SystemConnectorProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements SystemConnectorProvider for camunda.
 *
 * @author bbr
 */
public class SystemConnectorCamundaProviderImpl implements SystemConnectorProvider {

  @Override
  public List<SystemConnector> create() {
    // note: this class is created by ServiceLoader, not by Spring. Therefore it is no bean and we
    // must
    // retrieve the Spring-generated Bean for camundaSystemUrls programatically. Only this bean has
    // the properties
    // resolved.
    // In order for this bean to be retrievable, the SpringContextProvider must already be
    // initialized.
    // This is assured via the
    // @DependsOn(value= {"adapterSpringContextProvider"}) annotation of
    // CamundaSystemConnectorConfiguration

    CamundaSystemUrls camundaSystemUrls =
        AdapterSpringContextProvider.getBean(CamundaSystemUrls.class);

    List<SystemConnector> result = new ArrayList<>();
    for (CamundaSystemUrls.SystemUrlInfo camundaSystemUrl : camundaSystemUrls.getUrls()) {
      result.add(new CamundaSystemConnectorImpl(camundaSystemUrl));
    }

    return result;
  }
}
