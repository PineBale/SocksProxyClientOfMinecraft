plugins {
    java
    id("net.fabricmc.fabric-loom")
    id("com.gradleup.shadow")
    id("properties")
}

project.group = "${project.extra["maven_group"]}"

repositories {
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://maven.isxander.dev/releases/")
}

java {
    sourceCompatibility = JavaVersion.toVersion("${project.extra["compilation_java_version"]}")
    targetCompatibility = JavaVersion.toVersion("${project.extra["compilation_java_version"]}")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(Integer.valueOf("${project.extra["compilation_java_version"]}"))
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-proc:full", "-Werror", "-Xlint:-unchecked"))
}

tasks.withType<ProcessResources>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.FAIL
}

dependencies {
    minecraft("com.mojang:minecraft:${rootProject.findProperty("minecraft_version")}")
    implementation("net.fabricmc:fabric-loader:${rootProject.findProperty("fabric_loader_version")}")
}
