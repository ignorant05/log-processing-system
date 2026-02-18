package com.github.ignorant05.log_processing_system.service;

import com.github.ignorant05.log_processing_system.check.HealthCheckResult;
import com.github.ignorant05.log_processing_system.model.HealthCheck;
import com.github.ignorant05.log_processing_system.model.HealthStatus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** HealthService */
public class HealthService {
  private List<HealthCheck> checks = new ArrayList<>();

  public HealthService register(HealthCheck check) {
    this.checks.add(check);
    return this;
  }

  public HealthReport run() {
    List<HealthCheckResult> results = new ArrayList<>();
    for (HealthCheck check : checks) {
      results.add(check.check());
    }

    HealthStatus overall = HealthStatus.HEALTHY;
    for (HealthCheckResult result : results) {
      if (result.getStatus() == HealthStatus.UNHEALTHY) {
        overall = HealthStatus.UNHEALTHY;
        break;
      } else if (result.getStatus() == HealthStatus.DEGRADED) {
        overall = HealthStatus.DEGRADED;
      }
    }

    return new HealthReport(overall, Collections.unmodifiableList(results));
  }

  public static class HealthReport {
    private final HealthStatus overallStatus;
    private final List<HealthCheckResult> results;

    public HealthReport(HealthStatus overallStatus, List<HealthCheckResult> results) {
      this.overallStatus = overallStatus;
      this.results = results;
    }

    public HealthStatus getHealthStatus() {
      return this.overallStatus;
    }

    public List<HealthCheckResult> getResults() {
      return this.results;
    }

    public boolean isHealthy() {
      return overallStatus == HealthStatus.HEALTHY;
    }
  }
}
