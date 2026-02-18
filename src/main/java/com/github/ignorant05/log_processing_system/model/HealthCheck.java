package com.github.ignorant05.log_processing_system.model;

import com.github.ignorant05.log_processing_system.check.HealthCheckResult;

/** HealthCheck */
public interface HealthCheck {
  public String getName();

  public HealthCheckResult check();
}
