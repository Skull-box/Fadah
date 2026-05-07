@file:Suppress("UnstableApiUsage")

dependencyResolutionManagement {
    repositories {
        maven { url = uri("$rootDir/../libs/maven-repo") }
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }

    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "buildSrc"