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

// The version of the mod, the value of C0DE_VERSION (task 14.9)
val modVersion = Regex("""(?m)^mod\.version\s*=\s*"([^"]+)"""")
    .find(rootProject.file("stonecutter.properties.toml").readText())!!
    .groupValues[1]

tasks.test {
    useJUnitPlatform()
    systemProperty("c0de.version", modVersion)
}

// The program runner (task 14.1): the interpreter with SnakeYAML Engine in one jar, attached to the GitHub releases
// as c0de-runner-<version>.jar. `java -jar c0de-runner.jar program.c0de` (see runner/Main.java).
val runnerJar = tasks.register<Jar>("runnerJar") {
    group = "build"
    description = "Builds the program runner c0de-runner-<version>.jar"
    archiveFileName = "c0de-runner-$modVersion.jar"
    destinationDirectory = layout.buildDirectory.dir("runner")
    manifest {
        attributes(
            "Main-Class" to "de.joernott.c0de.core.runner.Main",
            "Implementation-Title" to "c0de-runner",
            "Implementation-Version" to modVersion,
        )
    }
    from(sourceSets.main.map { it.output })
    from(configurations.runtimeClasspath.map { classpath -> classpath.map { zipTree(it) } }) {
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/versions/*/module-info.class", "module-info.class")
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.assemble {
    dependsOn(runnerJar)
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
