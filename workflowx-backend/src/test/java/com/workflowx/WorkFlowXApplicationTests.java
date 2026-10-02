package com.workflowx;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * WorkFlowXApplicationTests — Context load test.
 *
 * This test verifies that the Spring ApplicationContext loads successfully.
 * If ANY bean fails to initialize (bad config, missing dependency, etc.),
 * this test will fail — giving us early warning of configuration problems.
 *
 * @SpringBootTest loads the full ApplicationContext (all beans, configs).
 * @ActiveProfiles("local") tells Spring to use application-local.yml overrides.
 *
 * This is the most basic but also most important test — it catches:
 *   - Circular dependencies
 *   - Missing required properties
 *   - Invalid configuration
 *   - Bean creation failures
 */
@SpringBootTest
@ActiveProfiles("local")
class WorkFlowXApplicationTests {

    @Test
    void contextLoads() {
        // If the context loads without throwing, this test passes.
        // No assertions needed — the test itself IS the assertion.
    }
}
