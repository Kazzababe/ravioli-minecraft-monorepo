plugins {
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "ravioli.gravioli"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven("https://repo.clojars.org")
    maven("https://jitpack.io")
}

dependencies {
    implementation("redis.clients:jedis:5.0.0-alpha2")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("org.mybatis:mybatis:3.5.13")
    implementation("net.kyori:adventure-platform-bukkit:4.3.0")
    implementation("net.kyori:adventure-text-minimessage:4.14.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.github.Carleslc.Simple-YAML:Simple-Yaml:1.8.4")
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }
}

publishing {
    publications {
        register<MavenPublication>("shadow") {
            project.shadow.component(this)
        }
    }
}