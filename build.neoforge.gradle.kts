plugins {
    id("net.neoforged.moddev") version "2.0.148"
    id("neoforge-mutex")
    id("me.modmuss50.mod-publish-plugin") version "2.2.1"
}

version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = "${property("mod.id") as String}-neoforge"

val requiredJava = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    else -> JavaVersion.VERSION_21
}

val modId = property("mod.id") as String

repositories {
    mavenCentral()
    maven("https://maven.blamejared.com") { name = "BlameJared" }
}

dependencies {
    // Patchouli is optional: with it, the guide book of the mod can be crafted and read
    runtimeOnly("vazkii.patchouli:Patchouli:${sc.properties.get<String>("deps.patchouli")}-NEOFORGE")
    // Used by core (TO_YAML, FROM_YAML, FROM_JSON), embedded in the mod jar
    implementation("org.snakeyaml:snakeyaml-engine:3.2")
    jarJar("org.snakeyaml:snakeyaml-engine:[3.2,4.0)") {
        version {
            prefer("3.2")
        }
    }
}

// The interpreter is compiled into every node
sourceSets.main {
    java.srcDir(rootProject.file("core/src/main/java"))
}

neoForge {
    version = property("deps.neo_loader") as String

    sc.properties.getOrNull<String>("deps.parchment")?.let {
        parchment {
            minecraftVersion = sc.current.version
            mappingsVersion = it
        }
    }

    mods {
        register(modId) {
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        register("client") {
            gameDirectory = rootProject.file("run/neoforge")
            client()
            // Fixed user name for multiplayer tests (task 11.11), so that operator rights on the test servers stay
            programArguments.addAll("--username", "Tester1")
        }

        // A second client for multiplayer tests (task 11.11): its own game directory and user name
        register("client2") {
            gameDirectory = rootProject.file("run/neoforge-client2")
            client()
            programArguments.addAll("--username", "Tester2")
        }

        register("server") {
            gameDirectory = rootProject.file("run/neoforge")
            server()
        }

        register("gameTestServer") {
            type = "gameTestServer"
            gameDirectory = file("build/gametest")
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
        }
    }
}

// Created by ModDevGradle once the NeoForge version is set: libraries for development runs, which are not
// mods (the jar-in-jar library is only unpacked in production)
dependencies {
    "additionalRuntimeClasspath"("org.snakeyaml:snakeyaml-engine:3.2")
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

tasks {
    processResources {
        fun MutableMap<String, String>.register(key: String, value: String) {
            inputs.property(key, value)
            set(key, value)
        }

        val props = buildMap {
            register("id", sc.properties["mod.id"])
            register("name", sc.properties["mod.name"])
            register("version", sc.properties["mod.version"])
            register("description", sc.properties["mod.description"])
            register("authors", sc.properties["mod.authors"])
            register("license", sc.properties["mod.license"])
            register("sources", sc.properties["mod.sources_url"])
            register("issues", sc.properties["mod.issues_url"])
            register("minecraft", sc.properties["mod.mc_compat"])
        }

        filesMatching("META-INF/neoforge.mods.toml") { expand(props) }

        exclude("fabric.mod.json", "c0de-fabric.mixins.json")
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }

    // Includes the license file in the built mod
    withType<Jar> {
        val id = project.property("mod.id") as String
        inputs.property("mod_id", id)
        from(rootProject.file("LICENSE")) { rename { "${it}_$id" } }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"

        inputs.property("version", project.property("mod.version"))
        from(jar.flatMap { it.archiveFile }, named<Jar>("sourcesJar").flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    }
}

// Publishing to Modrinth (task 11.12): `publishMods` is a dry run unless MODRINTH_TOKEN and the project ID are set
publishMods {
    val modVersion = property("mod.version") as String
    val projectId = sc.properties.get<String>("publish.modrinth_id")
    val token = providers.environmentVariable("MODRINTH_TOKEN")
    file = tasks.jar.flatMap { it.archiveFile }
    version = "${modVersion}+${sc.current.version}-neoforge"
    displayName = "c0de $modVersion for NeoForge ${sc.current.version}"
    changelog = providers.fileContents(rootProject.layout.projectDirectory.file("CHANGELOG.md")).asText
        .map { changelogSection(it, modVersion) }
    type = if (modVersion.startsWith("0.")) ALPHA else STABLE
    modLoaders.add("neoforge")
    dryRun = token.map { it.isBlank() || projectId.isBlank() }.orElse(true)
    modrinth {
        this.projectId = projectId
        accessToken = token
        minecraftVersions.addAll(sc.properties.get<String>("mod.mc_releases").split(",").map { it.trim() })
        optional("patchouli")
    }
}
