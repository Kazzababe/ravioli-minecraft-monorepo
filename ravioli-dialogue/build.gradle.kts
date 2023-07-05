plugins {
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.papermc.paperweight.userdev") version "1.3.8"
}

group = "ravioli.gravioli"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven("https://maven.citizensnpcs.co/repo")
    maven("https://repo.dmulloy2.net/repository/public/")
}

dependencies {
    compileOnly(project(path = ":ravioli-core", configuration = "shadow"))
    compileOnly("net.citizensnpcs:citizensapi:2.0.30-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.0.0")

    paperweightDevelopmentBundle("io.papermc.paper:dev-bundle:${rootProject.findProperty("paperVersion") as String}")
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }
}