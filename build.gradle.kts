plugins {
    java
}

group = "ravioli.gravioli"
version = "1.0-SNAPSHOT"

tasks.register("buildMinecraft") {
    subprojects.forEach { project ->
        if (project.findProperty("minecraft") == "true") {
            dependsOn("${project.path}:build")

            println("Building project ${project.name}.")
        }
    }
}

subprojects {
    apply(plugin = "java")

    dependencies {
        implementation("org.jetbrains:annotations:23.0.0")
    }

    plugins.withType<JavaPlugin> {
        java {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(17))
            }
        }
    }
}

tasks {
    named<Jar>("jar") {
        enabled = false
    }
    named("build") {
        enabled = false
    }
}