plugins {
    id("org.springframework.boot") version "4.2.0"

    kotlin("jvm") version "2.4.21"
    kotlin("plugin.spring") version "2.4.21"
    id("org.jetbrains.kotlin.plugin.noarg") version "2.4.21"

    id("com.diffplug.spotless") version "8.11.0"
    id("com.gorylenko.gradle-git-properties") version "4.0.1"
    jacoco
    id("io.github.chiragji.jacotura") version "1.1.2"
}

group = "org.veo"
version = "0.81.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))

    // security overrides
    implementation(platform("com.fasterxml.jackson:jackson-bom:2.23.0"))
    implementation(platform("tools.jackson:jackson-bom:3.3.0"))
    constraints {
        implementation("com.rabbitmq:amqp-client:5.38.0")
    }

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.hibernate.validator:hibernate-validator")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.github.oshai:kotlin-logging-jvm:8.0.7")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.1")
    implementation("org.springframework.boot:spring-boot-starter-rabbitmq")

    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("ch.qos.logback.contrib:logback-json-classic:0.1.5")
    runtimeOnly("ch.qos.logback.contrib:logback-jackson:0.1.5")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")

    val kotestVersion = "6.2.7"
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-property-jvm:$kotestVersion")

    testImplementation("io.mockk:mockk:1.14.12")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testRuntimeOnly("org.testcontainers:testcontainers-postgresql")
    testRuntimeOnly("org.springframework.boot:spring-boot-starter-security-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

kotlin {
    compilerOptions {
        allWarningsAsErrors = true
        freeCompilerArgs = listOf("-Xjsr305=strict")
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
