@file:Suppress("UNCHECKED_CAST")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.util.Optional

plugins {
    id("java")
    id("scala")
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("de.undercouch.download") version "5.6.0"
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.runelite.net")
    }
    gradlePluginPortal()
    mavenCentral()
}

val runeLiteVersion = "latest.release"
val lucidVersion = "6.6.6-all"
val ethanVersion = "5.4"

/**
 * Download all files from a directory in GitHub. Use the GitHub API to get the
 * directory's contents. Parse the result and download the files.
 */
tasks.register("downloadLucid") {
    // download directory listing via GitHub API
    val urlBase = "https://api.github.com/repos/lucid-plugins/SideloadPlugins/contents"
//        val dir = "release"
    val releaseContentFile = layout.buildDirectory.file("lucid_repo_contents.json").get().asFile
    download.run {
        src("${urlBase}/release")
        dest(releaseContentFile)
        onlyIfNewer(true)
    }

    // parse directory listing
    val contents = groovy.json.JsonSlurper().parse(releaseContentFile, "utf-8") as List<Map<String?, String?>>
    val libsFolder = layout.projectDirectory.dir("libs")

    val urls = contents.map {
        it["name"] to it["download_url"]
    }.flatMap {
        val cleanName = it.first?.removeSuffix(".jar")?.removeSuffix("-all")?.split("-", limit = 2).orEmpty()
//            println(cleanName)
//            cleanName?.last.isDigit()
//            if(cleanName)
//            endsWith("-all.jar") || it.first.endsWith()
//            if(cleanName)
        if (cleanName.size == 2) listOf(it.second) else emptyList()
    }
//        println(urls)
    // download files
    download.run {
        src(urls)
        dest(libsFolder)
    }
//
//        // delete downloaded directory listing
    val res = libsFolder.asFileTree.toList().groupBy({ it.nameWithoutExtension.split("-", limit = 2).first() },
        { it.nameWithoutExtension.split("-", limit = 2).drop(1).first() })
    res.forEach {
        println(it)
    }
        releaseContentFile.delete()
}

dependencies {
//    compileOnly(project)
//    compileOnly(files("https://github.com/lucid-plugins/SideloadPlugins/raw/master/release/EthanVannPlugins-${ethanVersion}.jar"))
    compileOnly(files("libs/LucidPlugins-${lucidVersion}.jar"))
    compileOnly(files("libs/EthanVannPlugins-${ethanVersion}.jar"))
//    compileOnly(files("release/LucidPlugins-6.6.1-all.jar"))
    compileOnly("org.projectlombok:lombok:1.18.20")
    compileOnly("net.runelite:client:$runeLiteVersion")
    compileOnly("org.pf4j:pf4j:3.6.0")
    annotationProcessor("org.projectlombok:lombok:1.18.20")
    testImplementation("junit:junit:4.13.1")

    implementation("org.scala-lang:scala3-library_3:3.4.2")
}

group = "com.fredplugins"
version = "0.2"

val javaMajorVersion = JavaVersion.VERSION_11.majorVersion

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaMajorVersion))
//        languageVersion.set(JavaLanguageVersion.of(8))
//        targetCompatibility
    }
}
tasks {
    withType<ScalaCompile> {
        println("scalaCompile")
        outputs.files.forEach { f ->
            println("\t" + f)
        }
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
//        sourceCompatibility = javaMajorVersion
//        targetCompatibility = javaMajorVersion
        println("javaCompile")
        outputs.files.forEach { f ->
            println("\t" + f)
        }
    }
    withType<Jar> {
        manifest {

        }
    }
    withType<ShadowJar> {
        baseName = "FredPlugins"
//        exclude("com/lucidplugins/lucidfletching/")
    }
}
