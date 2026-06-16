package com.enterpriserag.arch;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.enterpriserag")
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule domain_must_not_depend_on_adapters =
            noClasses()
                    .that().resideInAPackage("com.enterpriserag.domain..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("com.enterpriserag.adapter..");

    @ArchTest
    static final ArchRule domain_must_not_depend_on_framework =
            noClasses()
                    .that().resideInAPackage("com.enterpriserag.domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework..",
                            "jakarta.persistence..",
                            "org.apache.kafka..",
                            "dev.langchain4j.."
                    );

    @ArchTest
    static final ArchRule application_must_not_depend_on_adapters =
            noClasses()
                    .that().resideInAPackage("com.enterpriserag.application..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("com.enterpriserag.adapter..");

    @ArchTest
    static final ArchRule adapters_must_not_depend_on_bootstrap =
            noClasses()
                    .that().resideInAPackage("com.enterpriserag.adapter..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("com.enterpriserag")
                    .andShould().dependOnClassesThat()
                    .haveSimpleNameEndingWith("Application");
}
