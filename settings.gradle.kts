rootProject.name = "Fadah"
include("Bukkit", "API")

pluginManagement {
    repositories {
        maven { url = uri("$rootDir/libs/maven-repo") }
        mavenLocal()
        gradlePluginPortal()
    }
}