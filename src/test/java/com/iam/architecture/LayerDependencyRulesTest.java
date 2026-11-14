package com.iam.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

class LayerDependencyRulesTest {

  private static final JavaClasses CLASSES = ProductionClasses.ALL;

  @Test
  @DisplayName("should keep domain free of outer layers when a feature is built")
  void shouldKeepDomainFreeOfOuterLayers() {
    noClasses()
        .that()
        .resideInAPackage("com.iam..domain..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "com.iam..controller..", "com.iam..service..", "com.iam..infra..", "com.iam..mapper..")
        .because(
            "domain is the core every other layer depends on and must stay testable alone."
                + " Fix: declare an interface in <feature>/domain/repository and implement it"
                + " in <feature>/infra, or move the logic up into <feature>/service")
        .check(CLASSES);
  }

  @Test
  @DisplayName("should not reach controllers when service or infra code runs")
  void shouldNotReachControllersFromServiceOrInfra() {
    noClasses()
        .that()
        .resideInAnyPackage("com.iam..service..", "com.iam..infra..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("com.iam..controller..")
        .because(
            "controllers are the outermost layer and their DTOs are an HTTP contract."
                + " Fix: pass domain types or a service-level record instead of"
                + " controller DTOs, and map in <feature>/mapper or the controller")
        .check(CLASSES);
  }

  @Test
  @DisplayName("should not register domain types as Spring beans")
  void shouldNotRegisterDomainTypesAsSpringBeans() {
    noClasses()
        .that()
        .resideInAPackage("com.iam..domain..")
        .should()
        .beAnnotatedWith(Service.class)
        .orShould()
        .beAnnotatedWith(Component.class)
        .because(
            "domain objects are plain models; wiring belongs to the application layer."
                + " Fix: move the bean to <feature>/service, or to <feature>/infra if it"
                + " implements a domain repository")
        .check(CLASSES);
  }

  @Test
  @DisplayName("should keep feature modules free of dependency cycles")
  void shouldKeepFeatureModulesFreeOfDependencyCycles() {
    slices()
        .matching("com.iam.(*)..")
        .should()
        .beFreeOfCycles()
        .because(
            "a cycle means neither module can change or be tested alone."
                + " Fix: move the shared type to com.iam.common, or publish a domain event"
                + " instead of calling back")
        .check(CLASSES);
  }

  @Test
  @DisplayName("should reach audit only through domain events")
  void shouldReachAuditOnlyThroughDomainEvents() {
    noClasses()
        .that()
        .resideOutsideOfPackage("com.iam.audit..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("com.iam.audit..")
        .because(
            "audit must record every module without any module depending on it."
                + " Fix: publish an event from <feature>/domain and handle it with"
                + " @EventListener in com.iam.audit")
        .check(CLASSES);
  }
}
