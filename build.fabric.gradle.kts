plugins {
    // Applies the right Loom variant depending on the Minecraft version
    id("dev.kikugie.loom-back-compat")
    id("me.modmuss50.mod-publish-plugin") version "2.2.1"
}

// DO NOT set group = ...!
version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = "${property("mod.id") as String}-fabric"

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    else -> JavaVersion.VERSION_21
}

// The development runs of 1.21.1 keep their directories (run/fabric, run/neoforge); the other versions get
// their own (e.g. run/fabric-1.21.11), because mods in the mods folders work for one version only
val runSuffix = if (sc.current.version == "1.21.1") "" else "-${sc.current.version}"

repositories {
    maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
    maven("https://maven.blamejared.com") { name = "BlameJared" }
}

// The interpreter is compiled into every node
sourceSets.main {
    java.srcDir(rootProject.file("core/src/main/java"))
    // Data whose format changed between the Minecraft versions (buildSrc/VersionedResources.kt)
    versionedResourceDirs(rootProject.file("src/main/versioned")) { sc.current.parsed >= it }
        .forEach { resources.srcDir(it) }
}

dependencies {
    /**
     * Fetches only the required Fabric API modules.
     * @see <a href="https://github.com/FabricMC/fabric">List of Fabric API modules</a>
     */
    fun fapi(vararg modules: String) {
        for (it in modules) modImplementation(fabricApi.module(it, sc.properties["deps.fabric_api"]))
    }

    minecraft("com.mojang:minecraft:${sc.current.version}")
    val parchment = sc.properties.getOrNull<String>("deps.parchment")
    if (parchment != null) {
        mappings(loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-${sc.current.version}:$parchment@zip")
        })
    } else {
        // Applies Mojang mappings on obfuscated versions, does nothing on 26.1+
        loomx.applyMojangMappings()
    }

    // Use `mod{dependency type}` even on 26.1+ - loom-back-compat converts them
    modImplementation("net.fabricmc:fabric-loader:${sc.properties.get<String>("deps.fabric_loader")}")
    if (sc.current.parsed > "1.21.1") {
        // Fabric API renamed and merged modules after 1.21.1; newer nodes compile against all of it
        modImplementation("net.fabricmc.fabric-api:fabric-api:${sc.properties.get<String>("deps.fabric_api")}")
    } else fapi(
        "fabric-api-base",
        "fabric-lifecycle-events-v1",
        "fabric-resource-loader-v0",
        "fabric-gametest-api-v1",
        "fabric-rendering-v1",
        "fabric-networking-api-v1",
        "fabric-object-builder-api-v1",
        "fabric-blockrenderlayer-v1",
        "fabric-screen-handler-api-v1",
        "fabric-events-interaction-v0",
        "fabric-model-loading-api-v1",
    )
    // Used by core (TO_YAML, FROM_YAML, FROM_JSON), embedded in the mod jar
    include(implementation("org.snakeyaml:snakeyaml-engine:3.2")!!)

    // Players install the complete Fabric API, which fabric.mod.json requires. Development runs need it too.
    modLocalRuntime("net.fabricmc.fabric-api:fabric-api:${sc.properties.get<String>("deps.fabric_api")}")
    // Patchouli is optional: with it, the guide book of the mod can be crafted and read
    // Development runs without Patchouli: -Ppatchouli=false (task 11.11.4)
    if (providers.gradleProperty("patchouli").orNull != "false") {
        // Not every Minecraft version has a Patchouli release
        sc.properties.getOrNull<String>("deps.patchouli")?.let { modLocalRuntime("vazkii.patchouli:Patchouli:$it-FABRIC") }
    }
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")
    // Named mappings before 26.1, official (unobfuscated) names since then (src/main/versioned/accesswidener)
    accessWidenerPath = versionedResourceDirs(rootProject.file("src/main/versioned")) { sc.current.parsed >= it }
        .map { it.resolve("c0de.accesswidener") }
        .single { it.exists() }

    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1") // Adds names to lambdas - useful for mixins
    }

    runConfigs.all {
        preferGradleTask = true
        generateRunConfig = true
        runDirectory = rootProject.file("run/fabric$runSuffix")
        // The default heap is too small for 1.21.11 with JEI and Jade while terrain is generated
        jvmArguments.addAll("-Xms2G", "-Xmx6G")
    }

    runs {
        // Fixed user names for multiplayer tests (task 11.11), so that operator rights on the test servers stay
        named("client") {
            programArgs("--username", "Tester1")
        }
        // A second client for multiplayer tests (task 11.11): its own game directory and user name
        register("client2") {
            client()
            programArgs("--username", "Tester2")
            runDirectory = rootProject.file("run/fabric-client2$runSuffix")
        }
        register("gametest") {
            server()
            jvmArguments.add("-Dfabric-api.gametest")
            jvmArguments.add("-Dfabric-api.gametest.report-file=${project.layout.buildDirectory.get()}/reports/gametest/junit.xml")
            runDirectory = project.file("build/gametest")
        }
    }
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
            register("loader", Regex("\\d+\\.\\d+").find(sc.properties.get<String>("deps.fabric_loader"))!!.value)
        }

        filesMatching("fabric.mod.json") { expand(props) }

        exclude("META-INF/neoforge.mods.toml")
    }

    // Includes the license file in the built mod
    withType<Jar> {
        val name = project.property("mod.id")
        inputs.property("mod_id", name)
        from(rootProject.file("LICENSE")) { rename { "${it}_$name" } }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"

        inputs.property("version", project.property("mod.version"))
        // loomx.mod(Sources)Jar returns the jar task for the applied loom variant
        from(loomx.modJar.flatMap { it.archiveFile }, loomx.modSourcesJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    }
}

// Publishing to Modrinth (task 11.12): `publishMods` is a dry run unless MODRINTH_TOKEN and the project ID are set
publishMods {
    val modVersion = property("mod.version") as String
    val projectId = sc.properties.get<String>("publish.modrinth_id")
    val token = providers.environmentVariable("MODRINTH_TOKEN")
    file = loomx.modJar.flatMap { it.archiveFile }
    version = "${modVersion}+${sc.current.version}-fabric"
    displayName = "c0de $modVersion for Fabric ${sc.current.version}"
    changelog = providers.fileContents(rootProject.layout.projectDirectory.file("CHANGELOG.md")).asText
        .map { changelogSection(it, modVersion) }
    type = if (modVersion.startsWith("0.")) ALPHA else STABLE
    modLoaders.add("fabric")
    dryRun = token.map { it.isBlank() || projectId.isBlank() }.orElse(true)
    modrinth {
        this.projectId = projectId
        accessToken = token
        // c0de must be installed on clients and servers
        environment = CLIENT_AND_SERVER
        minecraftVersions.addAll(sc.properties.get<String>("mod.mc_releases").split(",").map { it.trim() })
        requires("fabric-api")
        optional("patchouli")
    }
}

// All compile errors at once, which helps when porting to a new Minecraft version
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-Xmaxerrs", "2000"))
}
