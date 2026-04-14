plugins {
    id("fundamental")
    id("git")
}

val baseName = "${project.extra["archives_base_name"]}"

setBaseVersion("${project.extra["mod_base_version"]}+${rootProject.findProperty("minecraft_version")}")
setBaseName(baseName)

repositories {
    maven("https://maven.nucleoid.xyz/")
}

dependencies {
    api("com.terraformersmc:modmenu:${rootProject.findProperty("modmenu_version")}") {
        exclude(group = "net.fabricmc.fabric-api", module = "fabric-api")
        exclude(group = "net.fabricmc", module = "fabric-loader")
    }

    shadow(implementation("io.netty:netty-handler-proxy:${project.extra["netty_version"]}") {
        exclude(group = "io.netty", module = "netty-common")
        exclude(group = "io.netty", module = "netty-buffer")
        exclude(group = "io.netty", module = "netty-transport")
        exclude(group = "io.netty", module = "netty-codec")
        exclude(group = "io.netty", module = "netty-handler")
    })
    shadow(implementation("dnsjava:dnsjava:${project.extra["dnsjava_version"]}") {
        exclude(group = "org.slf4j")
    })

    compileOnly("org.projectlombok:lombok:${project.extra["lombok_version"]}")
    annotationProcessor("org.projectlombok:lombok:${project.extra["lombok_version"]}")
}

tasks.register<GenerateConstantsClassTask>("generateConstantsClass") {
    className = "RevConstants"
    constants = mapOf(
        "mod_id" to project.extra["mod_id"],
        "mod_version" to project.version,
        "mod_name" to baseName,
        "mod_java_version" to project.extra["compilation_java_version"],
        "mod_dev_version" to project.extra["git_commit_sha"],
    )
    packageTarget = "${project.group}.fabric.socksproxyclient"
    outputDir = "${project.projectDir}/src/main/java/"
}

tasks.compileJava {
    dependsOn("generateConstantsClass")
}

loom {
    accessWidenerPath = file("src/main/resources/base.classtweaker")
    runs {
        getByName("client") {
            vmArgs.add("-Dfabric.development=true")
            vmArgs.add("-Dfabric.log.level=debug")
            vmArgs.add("-Dmixin.debug.strict=true")
            vmArgs.add("-Dmixin.debug.export=true")
            vmArgs.add("-Dmixin.debug.verify=true")
            vmArgs.add("-Dmixin.debug.countInjections=true")
        }
        remove(getByName("server"))
    }
}

tasks.runClient {
    dependsOn(tasks.build)
}

tasks.processResources {
    filesMatching("fabric.mod.json") {
        expand(
            mapOf(
                "mod_id" to project.extra["mod_id"],
                "mod_version" to project.version,
                "mod_name" to baseName,
                "minecraft_dependency" to rootProject.findProperty("minecraft_dependency"),
                "fabricloader_version" to rootProject.findProperty("fabric_loader_version"),
                "modmenu_version" to rootProject.findProperty("modmenu_version"),
            )
        )
    }
}

tasks.shadowJar {
    mergeServiceFiles()
    minimize()
    configurations = listOf(project.configurations.getByName("shadow"))
    archiveClassifier.set("")
    from(rootProject.rootDir) {
        include("LICENSE")
        rename { it -> "${it}_${baseName}" }
    }
}

tasks.build {
    dependsOn("shadowJar")
}
