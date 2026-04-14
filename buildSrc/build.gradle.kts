import java.util.Properties

plugins {
    `kotlin-dsl`
}

apply(from = "./src/main/kotlin/properties.gradle.kts")

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://maven.fabricmc.net/")
}

val rootProps = Properties().apply {
    load(file("../gradle.properties").inputStream())
}

java {
    sourceCompatibility = JavaVersion.toVersion("${project.extra["compilation_java_version"]}")
    targetCompatibility = JavaVersion.toVersion("${project.extra["compilation_java_version"]}")
}

kotlin {
    jvmToolchain(project.extra["compilation_java_version"].toString().toInt())
}

dependencies {
    implementation(gradleApi())
    implementation("com.squareup:javapoet:1.13.0")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("org.projectlombok:lombok:${project.extra["lombok_version"]}")
    implementation("net.fabricmc:fabric-loader:${rootProps.getProperty("fabric_loader_version")}")

    // Plugins:
    // net.fabricmc.fabric-loom
    // com.gradleup.shadow
    implementation("fabric-loom:fabric-loom.gradle.plugin:1.16-SNAPSHOT")
    implementation("com.gradleup.shadow:shadow-gradle-plugin:9.4.1")
}
