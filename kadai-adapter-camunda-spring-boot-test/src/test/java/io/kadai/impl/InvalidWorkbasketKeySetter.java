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

package io.kadai.impl;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for setting an invalid String variable "kadai.workbasket-key" in a test
 * scenario.
 */
public class InvalidWorkbasketKeySetter implements JavaDelegate {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkbasketKeySetter.class);

  @Override
  public void execute(DelegateExecution execution) {
    LOGGER.info("Setting workbasket key for testing purposes to \"invalidWorkbasketKey\" ");
    execution.setVariable("kadai.workbasket-key", "invalidWorkbasketKey");
  }
}