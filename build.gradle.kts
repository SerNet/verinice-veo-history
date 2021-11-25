import org.cadixdev.gradle.licenser.header.HeaderFormatRegistry
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Calendar

plugins {
    id("org.springframework.boot") version "2.6.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"

    kotlin("jvm") version "1.6.0"
    kotlin("plugin.spring") version "1.6.0"
    id("org.jetbrains.kotlin.plugin.noarg") version "1.6.0"

    id("com.diffplug.spotless") version "6.0.0"
    id("org.cadixdev.licenser") version "0.6.1"
    id("com.gorylenko.gradle-git-properties") version "2.3.1"
    jacoco
}

group = "org.veo"
version = "0.2"
java.sourceCompatibility = JavaVersion.VERSION_11

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
    implementation("org.postgresql:postgresql:42.3.1")
    implementation("com.vladmihalcea:hibernate-types-52:2.14.0")
    implementation("org.flywaydb:flyway-core:8.0.5")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.0")
    implementation("org.springdoc:springdoc-openapi-ui:1.5.12")
    implementation("io.mockk:mockk:1.12.1")
    implementation("org.springframework.boot:spring-boot-starter-amqp")

    val kotestVersion = "4.6.3"
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-property-jvm:$kotestVersion")

    testImplementation("org.codehaus.groovy:groovy-json:3.0.9")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        allWarningsAsErrors = true
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
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
