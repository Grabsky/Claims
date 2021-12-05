var buildsDirectory = "${System.getenv("IDEA_WORKSPACE")}/builds"

// Project values
group = "me.grabsky"
version = "1.0-SNAPSHOT"
description = "Claims"

// Defining Java version
java { toolchain.languageVersion.set(JavaLanguageVersion.of(17)) }

plugins {
    id("java-library")
    id("io.papermc.paperweight.userdev") version "1.3.1"
}

repositories {
    maven { url = uri("https://repo.maven.apache.org/maven2/") }
    maven { url = uri("https://papermc.io/repo/repository/maven-public/") }
    maven { url = uri("https://maven.enginehub.org/repo/") }
}

dependencies {
    paperDevBundle("1.18-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.8-SNAPSHOT")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.6")
    compileOnly(files(buildsDirectory + File.separator + "Indigo.jar"))
    compileOnly(files(buildsDirectory + File.separator + "Vanish.jar"))
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
                rename(reobfJar.get().outputJar.asFile.get().name, rootProject.name + ".jar")
            }
        }
    }
    compileJava { options.encoding = Charsets.UTF_8.name() }
    processResources { filteringCharset = Charsets.UTF_8.name() }
}