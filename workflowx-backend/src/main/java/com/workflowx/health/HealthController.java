package com.workflowx.health;

import com.workflowx.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * HealthController — Simple health check endpoint.
 *
 * WHY have a health endpoint?
 *
 * In production, load balancers, container orchestrators (Docker/Kubernetes),
 * and monitoring systems (Datadog, Grafana, UptimeRobot) periodically call
 * health endpoints to determine if the service is alive and should receive traffic.
 *
 * If the health check fails:
 *   - Load balancer removes the instance from the pool
 *   - Kubernetes restarts the pod
 *   - PagerDuty alerts on-call engineers
 *
 * For now, a simple "UP" response is sufficient.
 * In later phases, we can expand it to check DB connectivity, Redis, etc.
 *
 * @RestController = @Controller + @ResponseBody
 *   Every method return value is serialized directly to JSON response body.
 *   No need for @ResponseBody on individual methods.
 *
 * @RequestMapping("/api") → all methods in this controller are under /api
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * GET /api/health
     *
     * Returns:
     * {
     *   "success": true,
     *   "data": {
     *     "status": "UP",
     *     "application": "WorkFlowX"
     *   },
     *   "message": "Application is running"
     * }
     *
     * HTTP 200 OK
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        Map<String, String> healthData = Map.of(
                "status", "UP",
                "application", "WorkFlowX"
        );

        return ResponseEntity.ok(ApiResponse.success(healthData, "Application is running"));
    }
}
