plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.6"
}

group = "net.mircomacrelli"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.jthink:jaudiotagger:3.0.1")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "net.mircomacrelli.obsidian.musica.AlbumGenerator"
    }
}

tasks.shadowJar {
    archiveBaseName.set("obsidian-generators")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}