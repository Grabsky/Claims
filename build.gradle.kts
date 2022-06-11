val buildsDirectory = "${System.getenv("DEVELOPMENT_DIR")}/builds"

group = "claims"
version = "1.0-SNAPSHOT"
description = "Claims"

plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm") version "1.7.0"
    id("io.papermc.paperweight.userdev") version "1.3.6"
}

repositories {
    maven { url = uri("https://repo.maven.apache.org/maven2/") }
    maven { url = uri("https://papermc.io/repo/repository/maven-public/") }
    maven { url = uri("https://maven.enginehub.org/repo/") }
}

dependencies {
    // Kotlin (required)
    compileOnly("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", "1.7.0")
    compileOnly("org.jetbrains.kotlin", "kotlin-reflect", "1.7.0")
    // Paper (mojang mapped)
    paperDevBundle("1.19-R0.1-SNAPSHOT")
    // Dependencies
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.8-SNAPSHOT")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.6")
    compileOnly(files(buildsDirectory + File.separator + "Indigo.jar"))
    // compileOnly(files(buildsDirectory + File.separator + "Vanish.jar"))
}

tasks {
    build {
        dependsOn(reobfJar)
        doLast {
            // Copying output file to builds directory
            copy {
                from (reobfJar)
                into(buildsDirectory)
                // Renaming output file
                rename(reobfJar.get().outputJar.asFile.get().name, "${rootProject.name}.jar")
            }
        }
    }
    processResources { filteringCharset = Charsets.UTF_8.name() }
}