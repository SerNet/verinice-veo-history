plugins {
    id("org.springframework.boot") version "3.5.7"

    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("org.jetbrains.kotlin.plugin.noarg") version "2.2.21"

    id("com.diffplug.spotless") version "8.0.0"
    id("com.gorylenko.gradle-git-properties") version "2.5.3"
    jacoco
    id("io.github.chiragji.jacotura") version "1.1.2"
}

group = "org.veo"
version = "0.64.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.vladmihalcea:hibernate-types-60:2.21.1")
    implementation("org.flywaydb:flyway-core")
    implementation("org.hibernate.validator:hibernate-validator:9.0.1.Final")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14")
    implementation("org.springframework.boot:spring-boot-starter-amqp")

    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("ch.qos.logback.contrib:logback-json-classic:0.1.5")
    runtimeOnly("ch.qos.logback.contrib:logback-jackson:0.1.5")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")

    val kotestVersion = "6.0.4"
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-property-jvm:$kotestVersion")

    testImplementation("io.mockk:mockk:1.14.6")
    testImplementation("org.springframework.security:spring-security-test")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testRuntimeOnly("org.testcontainers:postgresql")
}

extra["kotlin-coroutines.version"] = "1.6.0"

tasks.withType<Test> {
    useJUnitPlatform()
}

kotlin {
    compilerOptions {
        allWarningsAsErrors = true
        freeCompilerArgs = listOf("-Xjsr305=strict ", "-Xannotation-default-target=param-property")
    }
}

// Add no-arg ORM constructors for JPA entities.
noArg {
    annotation("jakarta.persistence.Entity")
}

spotless {
    format("misc") {
        target("**/*.md", "**/*.gitignore")
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
    }
    kotlin {
        target("buildSrc/**/*.kt", "src/**/*.kt")
        addStep(
            org.veo.history.LicenseHeaderStep
                .create(project.rootDir),
        )
        ktlint()
    }
    kotlinGradle {
        ktlint()
    }
    json {
        target("**/*.json")
        gson().indentWithSpaces(2)
        endWithNewline()
    }
    yaml {
        target(".gitlab-ci.yml")
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
    }
}

springBoot {
    buildInfo {
        properties {
            if (rootProject.hasProperty("ciBuildNumber")) {
                additional.set(
                    mapOf(
                        "ci.buildnumber" to rootProject.properties["ciBuildNumber"] as String,
                        "ci.jobname" to rootProject.properties["ciJobName"] as String,
                    ),
                )
            }
        }
    }
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.name == "spring-data-jpa" && requested.version == "3.0.2") {
            useVersion("3.0.1")
            because("https://github.com/spring-projects/spring-data-jpa/issues/2812")
        }
    }
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        csv.required.set(true)
    }
}

jacotura {
    properties {
        property("jacotura.jacoco.path", "${layout.buildDirectory}/reports/jacoco/test/jacocoTestReport.xml")
        property("jacotura.cobertura.path", "${layout.buildDirectory}/reports/cobertura.xml")
    }
}
