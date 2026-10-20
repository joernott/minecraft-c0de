// The c0de interpreter: plain Java without any dependency on Minecraft or a mod loader.
// The mod nodes compile these sources directly (see build.fabric.gradle.kts and build.neoforge.gradle.kts),
// this project exists to test them in isolation.
//
// Formatting of the other files (mod sources with Stonecutter comments, build scripts) is checked by
// scripts/check-whitespace.sh, because Spotless only accepts files within its project directory.
plugins {
    `java-library`
    id("com.diffplug.spotless") version "8.10.4"
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        // The lowest Java version of all nodes (Minecraft 1.21.x)
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    // YAML 1.2 (TO_YAML, FROM_YAML) and JSON parsing (YAML 1.2 is a superset of JSON).
    // The mod nodes embed it with jar-in-jar.
    implementation("org.snakeyaml:snakeyaml-engine:3.2")

    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

// Formatting: `./gradlew spotlessCheck` (part of `check`) and `./gradlew spotlessApply`
spotless {
    // The interpreter is formatted completely
    java {
        target("src/**/*.java")
        palantirJavaFormat()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}
