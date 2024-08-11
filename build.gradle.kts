@file:Suppress("UNCHECKED_CAST")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.accessors.runtime.addDependencyTo
import org.jetbrains.kotlin.fir.declarations.builder.buildScript

plugins {
    id("java")
    id("scala")
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("de.undercouch.download") version "5.6.0"
    kotlin("jvm")
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
group = "com.fredplugins"
version = "0.3"

val javaMajorVersion = JavaVersion.VERSION_11.majorVersion
sourceSets {
    main {
        scala {
            setSrcDirs(listOf("src/main/ethans", "src/main/lucid", "src/main/plugins"))
        }
    }
}
//
//configurations["ethanCompileOnly"].extendsFrom(configurations.compileOnly.get())
//configurations["ethanImplementation"].extendsFrom(configurations.implementation.get())
//configurations["ethanRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())
//configurations["ethanAnnotationProcessor"].extendsFrom(configurations.annotationProcessor.get())
//
//configurations["pluginsCompileOnly"].extendsFrom(configurations.compileOnly.get())
//configurations["pluginsImplementation"].extendsFrom(configurations.implementation.get())
//configurations["pluginsRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())
//configurations["pluginsAnnotationProcessor"].extendsFrom(configurations.annotationProcessor.get())

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.20")
    compileOnly("org.projectlombok:lombok:1.18.20")
    compileOnly("net.runelite:client:$runeLiteVersion")
    compileOnly("org.pf4j:pf4j:3.6.0")
    compileOnly("org.scala-lang:scala3-library_3:3.4.2")
    compileOnly(kotlin("stdlib-jdk8"))

    implementation("org.json:json:20230227")
    implementation("org.roaringbitmap:RoaringBitmap:0.9.44")
    implementation("com.google.archivepatcher:archive-patch-applier:1.0.4")
    implementation("org.benf:cfr:0.152")

//    implementation(files("libs/LucidPlugins-${lucidVersion}.jar"))
    testImplementation("junit:junit:4.13.1")
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaMajorVersion))
    }
}

tasks {
    compileJava {
//        source(sourceSets.getByName("ethan"),sourceSets.getByName("plugins"), sourceSets.getByName("main"))
        options.encoding = "UTF-8"
        sourceCompatibility = javaMajorVersion
        targetCompatibility = javaMajorVersion
        println("javaCompile")
        inputs.sourceFiles.forEach {f ->
            println("\tIn: " + f)
        }
        println()
        outputs.files.forEach { f ->
            println("\tOut: " + f)
        }
    }
    compileScala {
//        source(sourceSets.getByName("ethan"),sourceSets.getByName("plugins"), sourceSets.getByName("main"))
        println("scalaCompile")
        inputs.sourceFiles.forEach {f ->
            println("\tIn: " + f)
        }
        println()
        outputs.files.forEach { f ->
            println("\tOut: " + f)
        }
    }
    jar {}
    shadowJar {
        this.dependsOn(jar)
        archiveClassifier.set("all")
        exclude("META-INF/versions/11/org/roaringbitmap/ArraysShim.class")
//        exclude("/com/lucidplugins/disablerendering/")
//        exclude("/com/lucidplugins/inferno/")
//        exclude("/com/lucidplugins/lucid1tkarambwans/")
//        exclude("/com/lucidplugins/lucidautodialog/")
//        exclude("/com/lucidplugins/lucidcannonreloader/")
//        exclude("/com/lucidplugins/lucidcombat/")
//        exclude("/com/lucidplugins/lucidcustomprayers/")
//        exclude("/com/lucidplugins/luciddiscordlogger/")
//        exclude("/com/lucidplugins/luciddukehelper/")
//        exclude("/com/lucidplugins/lucidgauntlet/")
//        exclude("/com/lucidplugins/lucidgearswapper/")
//        exclude("/com/lucidplugins/lucidhotkeys/")
//        exclude("/com/lucidplugins/lucidhotkeys2/")
//        exclude("/com/lucidplugins/lucidlevihelper/")
//        exclude("/com/lucidplugins/lucidmuspah/")
//        exclude("/com/lucidplugins/lucidpvpphelper/")
//        exclude("/com/lucidplugins/lucidscurriushelper/")
//        exclude("/com/lucidplugins/lucidspices/")
//        exclude("/com/lucidplugins/lucidtobprayers/")
//        exclude("/com/lucidplugins/lucidvardorvishelper/")
//        exclude("/com/lucidplugins/lucidwhispererhelper/")
//        exclude("/com/lucidplugins/lucidwildytele/")
//        exclude("/com/lucidplugins/oneclickagility/")
//        dependencies {
//            exclude {
//                val x = it.moduleGroup == "org.scala-lang" && it.moduleName == "scala3-library_3"
//                if(!x) {
//                    println("\n${it.moduleGroup}\n${it.moduleName}\n${it.moduleVersion}\n${it.name}\n")
//                }
//                x
//            }
//        }
    }

//    register<ShadowJar>("ethanShadowJar") {
//        from(sourceSets["ethan"].output)
//    }
//    register<ShadowJar>("pluginsShadowJar") {
//        from(sourceSets["plugins"].output)
//        archiveClassifier.set("all")
////        dependsOn("pluginsImplementation")
//    }
}
