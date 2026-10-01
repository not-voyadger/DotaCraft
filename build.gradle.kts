plugins {
    id("java-library")
    alias(libs.plugins.run.paper)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(zipTree(rootProject.file("run/paper-26.2-129.jar")))

    compileOnly("com.google.guava:guava:33.0.0-jre")
    compileOnly("org.jetbrains:annotations:24.1.0")
    compileOnly("net.kyori:adventure-api:4.17.0")
    compileOnly("com.mojang:brigadier:1.0.18")

    compileOnly(fileTree("libs") { include("*.jar") })
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks {
    compileJava {
        options.compilerArgs.add("-Xlint:none")
    }

    runServer {
        minecraftVersion("26.2")
        serverJar(rootProject.file("run/paper-26.2-129.jar"))
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}