package io.kadai.adapter.monitoring;

import io.kadai.adapter.impl.LastSchedulerRun;
import java.time.Duration;
import java.time.Instant;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class SchedulerHealthCheck implements HealthIndicator {

  private final LastSchedulerRun lastSchedulerRun;

  public SchedulerHealthCheck(LastSchedulerRun lastSchedulerRun) {
    this.lastSchedulerRun = lastSchedulerRun;
  }

  @Override
  public Health health() {

    if (lastSchedulerRun.getLastRunTime().isBefore(Instant.now().minus(Duration.ofMinutes(10)))) {
      return Health.down().build();
    }

    return Health.up().build();
  }
}
