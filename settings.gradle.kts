pluginManagement {
    repositories {
        gradlePluginPortal()

        maven("https://repo.papermc.io/repository/maven-public/")
    }
}
rootProject.name = "actual-monorepo"

include("ravioli-common")
include("ravioli-core")
include("ravioli-auction-house")
include("ravioli-mail")
include("ravioli-currency")
include("ravioli-custom-items")
include("ravioli-dialogue")
