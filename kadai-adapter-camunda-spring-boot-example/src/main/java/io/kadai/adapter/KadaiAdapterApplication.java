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

package io.kadai.adapter;

import io.kadai.adapter.configuration.AdapterConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/** Application that provides an adapter between KADAI and one or more external systems. */
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = "io.kadai.adapter")
@Import({AdapterConfiguration.class})
@EnableTransactionManagement
public class KadaiAdapterApplication {

  public static void main(String[] args) {
    SpringApplication.run(KadaiAdapterApplication.class, args);
  }

  // this method prevents checkstyle from thinking this class is a utility class
  public void dummy() {}
}