pluginManagement {
	repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
	}
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9"
}

stonecutter {
    create(rootProject) {
        // See https://stonecutter.kikugie.dev/wiki/start/#choosing-minecraft-versions
        versions("1.21.11").buildscript("build.legacy.gradle.kts")
        version("26.1").buildscript("build.modern.gradle.kts")
        vcsVersion = "26.1"
    }
}

rootProject.name = "MacOSInputFixes"
