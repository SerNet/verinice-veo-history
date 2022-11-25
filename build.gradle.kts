import org.cadixdev.gradle.licenser.header.HeaderFormatRegistry
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Calendar

plugins {
    id("org.springframework.boot") version "2.7.6"
    id("io.spring.dependency-management") version "1.1.0"

    kotlin("jvm") version "1.7.21"
    kotlin("plugin.spring") version "1.7.21"
    id("org.jetbrains.kotlin.plugin.noarg") version "1.7.21"

    id("com.diffplug.spotless") version "6.12.0"
    id("org.cadixdev.licenser") version "0.6.1"
    id("com.gorylenko.gradle-git-properties") version "2.4.1"
    jacoco
}

group = "org.veo"
version = "0.20-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.security:spring-security-test")
    implementation("org.postgresql:postgresql:42.5.1")
    implementation("com.vladmihalcea:hibernate-types-52:2.20.0")
    implementation("org.flywaydb:flyway-core:9.8.3")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")
    implementation("org.springdoc:springdoc-openapi-ui:1.6.13")
    implementation("io.mockk:mockk:1.13.2")
    implementation("org.springframework.boot:spring-boot-starter-amqp")

    val kotestVersion = "5.5.4"
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-property-jvm:$kotestVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}

extra["kotlin-coroutines.version"] = "1.6.0"

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        allWarningsAsErrors = true
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

// Add no-arg ORM constructors for JPA entities.
noArg {
    annotation("javax.persistence.Entity")
}

tasks.register("formatApply") {
    dependsOn("spotlessApply")
    dependsOn("licenseFormat")
}

spotless {
    format("misc") {
        target("**/*.md", "**/*.gitignore")
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
    kotlin {
        ktlint()
    }
    kotlinGradle {
        ktlint()
    }
}

license {
    header.set(resources.text.fromFile("templates/licenseHeader.txt"))
    newLine.set(false)
    skipExistingHeaders.set(true)
    exclude("**/*.properties")
    style(
        closureOf<HeaderFormatRegistry> {
            put("kt", "JAVADOC")
        }
    )
    ext["year"] = Calendar.getInstance().get(Calendar.YEAR)
    ext["author"] = ProcessBuilder("git", "config", "user.name").start()
        .inputStream.bufferedReader().readText().trim()
}

springBoot {
    buildInfo {
        properties {
            if (rootProject.hasProperty("ciBuildNumer")) {
                additional = mapOf(
                    "ci.buildnumber" to rootProject.properties["ciBuildNumer"],
                    "ci.jobname" to rootProject.properties["ciJobName"]
                )
            }
        }
    }
}

if (rootProject.hasProperty("ci")) {
    tasks.withType<Test> {
        // Don't let failing tests fail the build, let the junit step in the Jenkins pipeline decide what to do
        ignoreFailures = true
    }
}
