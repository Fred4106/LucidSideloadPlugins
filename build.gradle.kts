plugins {
//    id("java")
    id("scala")
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("de.undercouch.download") version "5.6.0"
}

repositories {
    maven {
        url = uri("https://repo.runelite.net")
    }
    gradlePluginPortal()
    mavenCentral()
}

val runeLiteVersion = "1.10.36.1"

/**
 * Download all files from a directory in GitHub. Use the GitHub API to get the
 * directory's contents. Parse the result and download the files.
 */
/* tasks.register("downloadLucid") {
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
*/
group = "com.fredplugins"
version = "0.3"

val javaMajorVersion = JavaVersion.VERSION_11.majorVersion
sourceSets {
    main {
        scala {
            setSrcDirs(listOf(
                "src/main/ethans",
                "src/main/lucid",
                "src/main/plugins",
            ))
        }
    }
}

dependencies {
    annotationProcessor(dependencyNotation = "org.projectlombok:lombok:1.18.30")
    compileOnly("org.projectlombok:lombok:1.18.30")
    compileOnly("org.pf4j:pf4j:3.10.0")
    compileOnly("net.runelite:client:$runeLiteVersion")

    implementation("org.scala-lang:scala3-library_3:3.4.2")
//    implementation("org.scala-lang:scala3-staging_3:3.4.2")

    implementation("net.codingwell:scala-guice_3:7.0.0") {
//        exclude("")
        exclude("com.google.inject", "guice")
    }
    //    implementation("com.lihaoyi:ammonite_3.4.2:3.0.0-M2-15-9bed9700")
    implementation("org.json:json:20231013") //setTransitive(false) }
    implementation("org.roaringbitmap:RoaringBitmap:0.9.44")
//    implementation("com.google.archivepatcher:archive-patch-applier:1.0.4")
    implementation("org.benf:cfr:0.152")

//   implementation(files("libs/LucidPlugins-${lucidVersion}.jar"))
    testImplementation("junit:junit:4.13.1")
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaMajorVersion))
    }
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        sourceCompatibility = javaMajorVersion
        targetCompatibility = javaMajorVersion
    }
    compileScala {
        println("scalaCompile")
        inputs.sourceFiles.forEach { f ->
            println("\tIn: " + f)
        }
        println()
        outputs.files.forEach { f ->
            println("\tOut: " + f)
        }
    }
    jar {
    }
    shadowJar {
//        this.dependsOn(classes)
        System.out.println("inputs: " + inputs.files)
        duplicatesStrategy = DuplicatesStrategy.WARN
        archiveClassifier.set("all")
        exclude("META-INF/versions/11/org/roaringbitmap/ArraysShim.class")
    }
}
