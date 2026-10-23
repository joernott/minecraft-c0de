plugins {
    // Applies the right Loom variant depending on the Minecraft version
    id("dev.kikugie.loom-back-compat")
}

// DO NOT set group = ...!
version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = "${property("mod.id") as String}-fabric"

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    else -> JavaVersion.VERSION_21
}

repositories {
    maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
    maven("https://maven.blamejared.com") { name = "BlameJared" }
}

// The interpreter is compiled into every node
sourceSets.main {
    java.srcDir(rootProject.file("core/src/main/java"))
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
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    fapi(
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
    modLocalRuntime("vazkii.patchouli:Patchouli:${sc.properties.get<String>("deps.patchouli")}-FABRIC")
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")
    accessWidenerPath = rootProject.file("src/main/resources/c0de.accesswidener")

    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1") // Adds names to lambdas - useful for mixins
    }

    runConfigs.all {
        preferGradleTask = true
        generateRunConfig = true
        runDirectory = rootProject.file("run/fabric")
    }

    runs {
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
