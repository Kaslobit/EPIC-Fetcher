plugins {
    kotlin("jvm") version "2.1.20"
    id("com.gradleup.shadow") version "8.3.6"
    application
}

group = "me.kaslo.epicfetcher"
version = "1.0"

repositories {
    mavenCentral()
}

application {
    mainClass.set("me.kaslo.epicfetcher.MainKt")
}

dependencies {
    implementation(kotlin("stdlib"))
    // https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // https://mvnrepository.com/artifact/com.fasterxml.jackson.module/jackson-module-kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.3")
}

kotlin {
    jvmToolchain(21)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.shadowJar {
    archiveClassifier.set("")
}

// Everything below here is just to resolve some build errors
tasks.distZip {
    dependsOn("shadowJar")
}

tasks.distTar {
    dependsOn("shadowJar")
}

tasks.startScripts {
    dependsOn("shadowJar")
}

tasks.startShadowScripts {
    dependsOn("jar")
}
