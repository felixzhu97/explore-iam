package com.iam.common.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/** Enables {@code @PreAuthorize} on management API controllers. */
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {}
