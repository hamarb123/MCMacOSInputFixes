import org.gradle.jvm.tasks.Jar

plugins {
	id("net.fabricmc.fabric-loom-remap")
	id("maven-publish")
}

val modVersion = project.property("mod_version") as String
version = "$modVersion+${project.property("minecraft_version") as String}"
group = project.property("maven_group") as String

base {
	archivesName.set(project.property("archives_base_name") as String)
}

repositories {
	// Add repositories to retrieve artifacts from in here.
	// You should only use this when depending on other mods because
	// Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
	// See https://docs.gradle.org/current/userguide/declaring_repositories.html
	// for more information about repositories.
}

loom {
	splitEnvironmentSourceSets()

	mods {
		create("macos_input_fixes") {
			sourceSet(sourceSets.main.get())
			sourceSet(sourceSets.named("client").get())
		}
	}
}

sourceSets {
	named("client") {
		java {
			exclude("com/hamarb123/macos_input_fixes/client/mixin/KeyboardHandlerAccessor15.java")
		}
	}
}

dependencies {
	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${project.property("minecraft_version") as String}")
	mappings(loom.officialMojangMappings())
	modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version") as String}")

	// Uncomment the following line to enable the deprecated Fabric API modules. 
	// These are included in the Fabric API production distribution and allow you to update your mod to the latest modules at a later more convenient time.

	// modImplementation("net.fabricmc.fabric-api:fabric-api-deprecated:${project.fabric_version}")
}

tasks.processResources {
	val loaderDependency = ">=${project.property("loader_version") as String}"

	inputs.property("version", project.version)
	inputs.property("minecraft_dependency", project.property("minecraft_dependency") as String)
	inputs.property("loader_dependency", loaderDependency)

	filesMatching("fabric.mod.json") {
		expand(
			mapOf(
				"version" to project.version,
				"minecraft_dependency" to (project.property("minecraft_dependency") as String),
				"loader_dependency" to loaderDependency
			)
		)
	}

	filesMatching("macos_input_fixes.client.mixins.json") {
		expand(
			mapOf(
				"loader_dependency" to loaderDependency,
				"minecraft" to "${'$'}{minecraft}"
			)
		)
	}
}

tasks.withType<JavaCompile>().configureEach {
	// Java 8 for compatibility with all versions.
	options.release.set(8)
}

java {
	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
	// if it is present.
	// If you remove this line, sources will not be generated.
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.jar {
	inputs.property("archivesName", base.archivesName.get())

	from("LICENSE") {
		rename { "${it}_${inputs.properties["archivesName"]}"}
	}
}

// configure the maven publication
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifactId = project.property("archives_base_name") as String
			from(components["java"])
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		// Add repositories to publish to here.
		// Notice: This block does NOT have the same function as the block in the top level.
		// The repositories here will be used for publishing your artifact, not for
		// retrieving dependencies.
	}
}

// Builds the version into a shared folder in `build/libs/${mod version}/`
tasks.register<Copy>("buildAndCollect") {
	group = "build"
	from(tasks.named<Jar>("remapJar").map { it.archiveFile })
	into(rootProject.layout.buildDirectory.file("libs/${project.property("mod_version")}"))
	dependsOn("build")
}
