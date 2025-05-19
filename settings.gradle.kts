pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("1.0.0")
}

rootProject.name = "veo-history"

val isCiServer = System.getenv().containsKey("CI")

buildCache {
    local {
        isEnabled = !isCiServer
    }
    System.getenv("GRADLE_REMOTE_BUILD_CACHE_URL")?.let { url ->
        remote<HttpBuildCache> {
            this.url = uri(url)
            isPush = isCiServer
            isAllowUntrustedServer = isCiServer
        }
    }
}
