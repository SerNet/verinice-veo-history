plugins {
        `kotlin-dsl`
    }

repositories {
        mavenCentral()
    }

dependencies{
        implementation("com.diffplug.spotless:spotless-lib-extra:4.11.0")
        implementation("org.eclipse.jgit:org.eclipse.jgit:7.8.1.202611041320-r")
    }
