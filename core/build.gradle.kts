// The c0de interpreter: plain Java without any dependency on Minecraft or a mod loader.
// The mod nodes compile these sources directly (see build.fabric.gradle.kts and build.neoforge.gradle.kts),
// this project exists to test them in isolation.
plugins {
    `java-library`
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
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
