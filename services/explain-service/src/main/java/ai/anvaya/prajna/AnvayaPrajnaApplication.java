package ai.anvaya.prajna;

import ai.anvaya.prajna.config.EnableExplanationEngine;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Example Reference REST Microservice for Anvaya-Prajna Explanation Engine.
 * Consumers can embed 'libraries:explain-core' directly with @EnableExplanationEngine
 * and implement their own authentication, RBAC, and gateway layers (ASI03).
 */
@SpringBootApplication
@EnableExplanationEngine
public class AnvayaPrajnaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnvayaPrajnaApplication.class, args);
    }
}
