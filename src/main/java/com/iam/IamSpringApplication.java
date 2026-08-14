package com.iam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Spring Boot entry point for Explore IAM. */
@SpringBootApplication
public class IamSpringApplication {

  /**
   * Starts the Explore IAM application.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(IamSpringApplication.class, args);
  }
}
