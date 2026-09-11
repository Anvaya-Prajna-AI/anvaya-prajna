package ai.anvaya.prajna.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enable the Anvaya-Prajna Explanation Engine in any consumer Spring Boot application.
 * Automatically scans components, domain plugins, security interceptors, JPA repositories, and entities.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Configuration
@ComponentScan(basePackages = "ai.anvaya.prajna")
@EntityScan(basePackages = "ai.anvaya.prajna.domain.entity")
@EnableJpaRepositories(basePackages = "ai.anvaya.prajna.repository")
public @interface EnableExplanationEngine {
}
