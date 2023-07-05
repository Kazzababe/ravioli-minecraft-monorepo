plugins {
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.papermc.paperweight.userdev") version "1.3.8"
}

group = "ravioli.gravioli"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }
}

dependencies {
    implementation(project(path =":ravioli-common", configuration = "shadow"))
    implementation("ravioli.gravioli:command-bukkit:1.0-SNAPSHOT")
    implementation("ravioli.gravioli:ravioli-gui:1.0-SNAPSHOT")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    paperweightDevelopmentBundle("io.papermc.paper:dev-bundle:${rootProject.findProperty("paperVersion") as String}")
}