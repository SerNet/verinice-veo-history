import com.diffplug.spotless.FormatterStep
import com.fasterxml.jackson.core.util.DefaultIndenter.SYSTEM_LINEFEED_INSTANCE
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.jk1.license.filter.LicenseBundleNormalizer
import com.github.jk1.license.render.TextReportRenderer
import com.github.jk1.license.task.ReportTask
import org.cadixdev.gradle.licenser.header.HeaderFormatRegistry
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Calendar

plugins {
    id("org.springframework.boot") version "3.0.2"
    id("io.spring.dependency-management") version "1.1.0"

    kotlin("jvm") version "1.8.10"
    kotlin("plugin.spring") version "1.8.10"
    id("org.jetbrains.kotlin.plugin.noarg") version "1.8.10"

    id("com.diffplug.spotless") version "6.14.0"
    id("org.cadixdev.licenser") version "0.6.1"
    id("com.github.jk1.dependency-license-report") version "2.1"
    id("com.gorylenko.gradle-git-properties") version "2.4.1"
    jacoco
}

group = "org.veo"
version = "0.22.0-SNAPSHOT"

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
    implementation("org.postgresql:postgresql:42.5.3")
    implementation("com.vladmihalcea:hibernate-types-60:2.21.1")
    implementation("org.flywaydb:flyway-core")
    implementation("org.hibernate.validator:hibernate-validator:8.0.0.Final")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.2")
    implementation("io.mockk:mockk:1.13.4")
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

val licenseFile3rdParty = "LICENSE-3RD-PARTY.txt"
licenseReport {
    renderers = arrayOf(
        TextReportRenderer(licenseFile3rdParty),
    )
    projects = arrayOf(project)
    filters = arrayOf(
        LicenseBundleNormalizer(),
    )
}

val reportTask = tasks.getByName("generateLicenseReport") as ReportTask
// work around for license report not being updated when the project's version number changes
// https://github.com/jk1/Gradle-License-Report/issues/223
reportTask.outputs.apply {
    upToDateWhen { false }
    cacheIf { false }
}
task("copy3rdPartyLicenseFile") {
    reportTask.finalizedBy(this)
    doLast {
        file(licenseFile3rdParty).writeText(file("${reportTask.config.outputDir}/$licenseFile3rdParty").readText())
    }
}

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
    annotation("jakarta.persistence.Entity")
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
    json {
        target("**/*.json")
        addStep(object : FormatterStep {
            override fun getName() = "format json"
            override fun format(rawUnix: String, file: File): String {
                val om = ObjectMapper()
                return om.writer()
                    .with(DefaultPrettyPrinter().apply { indentArraysWith(SYSTEM_LINEFEED_INSTANCE) })
                    .writeValueAsString(om.readValue(rawUnix, Map::class.java))
            }
        })
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
        },
    )
    ext["year"] = Calendar.getInstance().get(Calendar.YEAR)
    ext["author"] = ProcessBuilder("git", "config", "user.name").start()
        .inputStream.bufferedReader().readText().trim()
}

springBoot {
    buildInfo {
        properties {
            if (rootProject.hasProperty("ciBuildNumer")) {
                additional.set(
                    mapOf(
                        "ci.buildnumber" to rootProject.properties["ciBuildNumer"],
                        "ci.jobname" to rootProject.properties["ciJobName"],
                    ),
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
