package com.iam.common;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Architecture rules")
class ArchitectureRulesTest {

  private static final JavaClasses CLASSES = new ClassFileImporter().importPackages("com.iam");

  private static final JavaClasses PRODUCTION_CLASSES =
      new ClassFileImporter()
          .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
          .importPackages("com.iam");

  @Test
  @DisplayName("should forbid legacy domain port packages")
  void shouldForbidLegacyDomainPortPackages() {
    ArchRuleDefinition.noClasses().should().resideInAPackage("..domain.port..").check(CLASSES);
  }

  @Test
  @DisplayName("should forbid hexagonal adapter in out packages")
  void shouldForbidHexagonalAdapterPackages() {
    ArchRuleDefinition.noClasses()
        .should()
        .resideInAnyPackage("..adapter.in..", "..adapter.out..")
        .check(CLASSES);
  }

  @Test
  @DisplayName("should keep feature modules free of dependency cycles")
  void shouldKeepFeatureModulesFreeOfDependencyCycles() {
    SlicesRuleDefinition.slices()
        .matching("com.iam.(*)..")
        .should()
        .beFreeOfCycles()
        .check(PRODUCTION_CLASSES);
  }

  @Test
  @DisplayName("should reach audit only through domain events")
  void shouldReachAuditOnlyThroughDomainEvents() {
    ArchRuleDefinition.noClasses()
        .that()
        .resideOutsideOfPackage("com.iam.audit..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("com.iam.audit..")
        .check(PRODUCTION_CLASSES);
  }
}
