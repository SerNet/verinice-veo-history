import com.diffplug.spotless.FormatterStep
import com.fasterxml.jackson.core.util.DefaultIndenter.SYSTEM_LINEFEED_INSTANCE
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.jk1.license.filter.LicenseBundleNormalizer
import com.github.jk1.license.render.TextReportRenderer
import com.github.jk1.license.task.ReportTask
import org.cadixdev.gradle.licenser.header.HeaderFormatRegistry
import org.eclipse.jgit.api.Git
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Calendar
import kotlin.text.Regex
import kotlin.text.RegexOption

plugins {
    id("org.springframework.boot") version "3.3.1"
    id("io.spring.dependency-management") version "1.1.6"

    kotlin("jvm") version "2.0.0"
    kotlin("plugin.spring") version "2.0.0"
    id("org.jetbrains.kotlin.plugin.noarg") version "2.0.0"

    id("com.diffplug.spotless") version "6.25.0"
    id("org.cadixdev.licenser") version "0.6.1"
    id("com.github.jk1.dependency-license-report") version "2.8"
    id("com.gorylenko.gradle-git-properties") version "2.4.2"
    jacoco
    id("io.github.chiragji.jacotura") version "1.1.2"
}

group = "org.veo"
version = "0.43.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
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
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("com.vladmihalcea:hibernate-types-60:2.21.1")
    implementation("org.flywaydb:flyway-core")
    implementation("org.hibernate.validator:hibernate-validator:8.0.1.Final")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    implementation("org.springframework.boot:spring-boot-starter-amqp")

    runtimeOnly("ch.qos.logback.contrib:logback-json-classic:0.1.5")
    runtimeOnly("ch.qos.logback.contrib:logback-jackson:0.1.5")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")

    val kotestVersion = "5.9.1"
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-property-jvm:$kotestVersion")

    testImplementation("io.mockk:mockk:1.13.11")
    testImplementation("org.springframework.security:spring-security-test")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}

extra["kotlin-coroutines.version"] = "1.6.0"

val licenseFile3rdParty = "LICENSE-3RD-PARTY.txt"
licenseReport {
    renderers =
        arrayOf(
            TextReportRenderer(licenseFile3rdParty),
        )
    projects = arrayOf(project)
    filters =
        arrayOf(
            LicenseBundleNormalizer(),
        )
}

tasks.withType<ReportTask> {
    outputs.apply {
        // work around for license report not being updated when the project's version number changes
        // https://github.com/jk1/Gradle-License-Report/issues/223
        upToDateWhen { false }
        cacheIf { false }
    }
    doLast {
        val dateLinePattern = Regex("^This report was generated at.+$", RegexOption.MULTILINE)
        val newLicenseText = file("${config.outputDir}/$licenseFile3rdParty").readText()
        val licenseFile = file(licenseFile3rdParty)
        if (licenseFile.readText().replace(dateLinePattern, "") != newLicenseText.replace(dateLinePattern, "")) {
            licenseFile.writeText(newLicenseText)
        }
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
        addStep(
            object : FormatterStep {
                override fun getName() = "format json"

                override fun format(
                    rawUnix: String,
                    file: File,
                ): String {
                    val om = ObjectMapper()
                    return om.writer()
                        .with(DefaultPrettyPrinter().apply { indentArraysWith(SYSTEM_LINEFEED_INSTANCE) })
                        .writeValueAsString(om.readValue(rawUnix, Map::class.java))
                }
            },
        )
    }
    yaml {
        target(".gitlab-ci.yml")
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
}

license {
    header.set(resources.text.fromFile("templates/licenseHeader.txt"))
    newLine.set(false)
    skipExistingHeaders.set(true)
    exclude("**/*.properties", "**/*.xml")
    style(
        closureOf<HeaderFormatRegistry> {
            put("kt", "JAVADOC")
        },
    )
    ext["year"] = Calendar.getInstance().get(Calendar.YEAR)
    ext["author"] =
        Git.open(project.rootDir).use {
            it.getRepository().getConfig().getString("user", null, "name") ?: "<name>"
        }
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
        property("jacotura.jacoco.path", "$buildDir/reports/jacoco/test/jacocoTestReport.xml")
        property("jacotura.cobertura.path", "$buildDir/reports/cobertura.xml")
    }
}
