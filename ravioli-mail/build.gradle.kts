plugins {
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.papermc.paperweight.userdev") version "1.3.8"
}

group = "ravioli.gravioli"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }
}

dependencies {
    compileOnly(project(path = ":ravioli-core", configuration = "shadow"))
    compileOnly(project(path = ":ravioli-custom-items", configuration = "shadow"))

    paperweightDevelopmentBundle("io.papermc.paper:dev-bundle:${rootProject.findProperty("paperVersion") as String}")
}