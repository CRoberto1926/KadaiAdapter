package io.kadai.adapter.impl;

import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class LastSchedulerRun {
  private Instant lastRunTime;

  public Instant getLastRunTime() {
    return lastRunTime;
  }

  void touch() {
    lastRunTime = Instant.now();
  }
}
