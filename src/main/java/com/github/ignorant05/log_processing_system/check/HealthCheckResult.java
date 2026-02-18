package com.github.ignorant05.log_processing_system.check;

import com.github.ignorant05.log_processing_system.model.HealthStatus;

/** HealthCheckResult */
public class HealthCheckResult {
  private final String name;
  private final HealthStatus status;
  private final String message;
  private final String detail;

  public HealthCheckResult(String name, HealthStatus status, String message, String detail) {
    this.name = name;
    this.status = status;
    this.message = message;
    this.detail = detail;
  }

  public static HealthCheckResult healthy(String name, String message) {
    return new HealthCheckResult(name, HealthStatus.HEALTHY, message, null);
  }

  public static HealthCheckResult degraded(String name, String message, String detail) {
    return new HealthCheckResult(name, HealthStatus.DEGRADED, message, detail);
  }

  public static HealthCheckResult unhealthy(String name, String message, String detail) {
    return new HealthCheckResult(name, HealthStatus.UNHEALTHY, message, detail);
  }

  public String getName() {
    return this.name;
  }

  public HealthStatus getStatus() {
    return this.status;
  }

  public String getMessage() {
    return this.message;
  }

  public String getDetail() {
    return this.detail;
  }
}
