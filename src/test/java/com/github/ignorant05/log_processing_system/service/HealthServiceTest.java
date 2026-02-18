package com.github.ignorant05.log_processing_system.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.ignorant05.log_processing_system.check.HealthCheckResult;
import com.github.ignorant05.log_processing_system.model.HealthCheck;
import com.github.ignorant05.log_processing_system.model.HealthStatus;
import java.util.List;
import org.junit.jupiter.api.Test;

/** HealthServiceTest */
public class HealthServiceTest {

  private HealthCheck check(String name, HealthCheckResult result) {
    return new HealthCheck() {
      @Override
      public String getName() {
        return name;
      }

      @Override
      public HealthCheckResult check() {
        return result;
      }
    };
  }

  @Test
  void allHealthy_shouldReturnHealthy() {
    HealthService service =
        new HealthService()
            .register(check("A", HealthCheckResult.healthy("A", "ok")))
            .register(check("B", HealthCheckResult.healthy("B", "ok")));

    HealthService.HealthReport report = service.run();

    assertEquals(HealthStatus.HEALTHY, report.getHealthStatus());
    assertTrue(report.isHealthy());
    assertEquals(2, report.getResults().size());
  }

  @Test
  void onDegraded_shouldReturnDegraded() {
    HealthService service =
        new HealthService()
            .register(check("A", HealthCheckResult.healthy("A", "ok")))
            .register(check("B", HealthCheckResult.degraded("B", "partial", "detail")));

    HealthService.HealthReport report = service.run();

    assertEquals(HealthStatus.DEGRADED, report.getHealthStatus());
    assertFalse(report.isHealthy());
  }

  @Test
  void onUnhealthy_shouldReturnUnhealthy_evenWithDegraded() {
    HealthService service =
        new HealthService()
            .register(check("A", HealthCheckResult.unhealthy("A", "hoho", "nothing")))
            .register(check("B", HealthCheckResult.degraded("B", "partial", "detail")));

    HealthService.HealthReport report = service.run();

    assertEquals(HealthStatus.UNHEALTHY, report.getHealthStatus());
  }

  @Test
  void noChecks_shouldReturnHealthy() {

    HealthService.HealthReport report = new HealthService().run();

    assertEquals(HealthStatus.HEALTHY, report.getHealthStatus());
    assertTrue(report.isHealthy());
    assertTrue(report.getResults().isEmpty());
  }

  @Test
  void results_preserveInsertionOrder() {
    HealthService service =
        new HealthService()
            .register(check("First", HealthCheckResult.healthy("First", "ok")))
            .register(check("Second", HealthCheckResult.unhealthy("Second", "down", "err")))
            .register(check("Third", HealthCheckResult.degraded("Third", "warn", null)));

    List<HealthCheckResult> results = service.run().getResults();

    assertEquals("First", results.get(0).getName());
    assertEquals("Second", results.get(1).getName());
    assertEquals("Third", results.get(2).getName());
  }

  @Test
  void multipleUnhealthy_shouldStillReturnUnhealthy() {
    HealthService service =
        new HealthService()
            .register(check("A", HealthCheckResult.unhealthy("A", "down", "err1")))
            .register(check("B", HealthCheckResult.unhealthy("B", "down", "err2")));

    assertEquals(HealthStatus.UNHEALTHY, service.run().getHealthStatus());
  }
}
