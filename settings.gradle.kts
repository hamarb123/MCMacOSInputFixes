pluginManagement {
	repositories {
		mavenLocal()
		mavenCentral()
		gradlePluginPortal()
		maven("https://maven.fabricmc.net/") { name = "Fabric" }
		maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
	}

	plugins {
		val loomVersion = providers.gradleProperty("loom_version").get()
		id("net.fabricmc.fabric-loom") version loomVersion
		id("net.fabricmc.fabric-loom-remap") version loomVersion
	}
}

plugins {
	id("dev.kikugie.stonecutter") version providers.gradleProperty("stonecutter_version").get()
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
