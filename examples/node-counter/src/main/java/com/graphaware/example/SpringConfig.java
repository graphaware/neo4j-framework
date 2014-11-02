package com.graphaware.example;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Picked up by GraphAware classpath scanning (because it lived in a package containing the String "graphaware" on path).
 * Enables the use of @Transactional.
 */
@Configuration
@EnableTransactionManagement
public class SpringConfig {
}
