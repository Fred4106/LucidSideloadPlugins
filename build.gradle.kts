import org.gradle.tooling.internal.protocol.ProjectVersion3

plugins {
    id("java")
    id("scala")
    id("com.github.johnrengelman.shadow") version "7.0.0"
//    id("de.undercouch.download") version "5.6.0"
}

val runeLiteVersion = "1.10.36.1"
val javaVersion = JavaVersion.VERSION_11
subprojects {
    apply(plugin="java-library")
}
allprojects {
    group = "com.fredplugins"
    version = "0.3"
    repositories {
        maven {
            url = uri("https://repo.runelite.net")
        }
        gradlePluginPortal()
        mavenCentral()
    }
    apply<MavenPublishPlugin>()

    dependencies {
        this.add("annotationProcessor", "org.projectlombok:lombok:1.18.30")
        this.add("compileOnly", "org.projectlombok:lombok:1.18.30")
        this.add("compileOnly", "org.pf4j:pf4j:3.10.0")
        this.add("compileOnly", "net.runelite:client:$runeLiteVersion")
        this.add("testImplementation", "junit:junit:4.13.1")
    }
    configure<JavaPluginExtension> {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
//        options.encoding = "UTF-8"
    }
}

dependencies {
    implementation(project(":ethans"))
    implementation(project(":lucid"))

    implementation("org.scala-lang:scala3-library_3:3.4.2")
    implementation("net.codingwell:scala-guice_3:7.0.0") {
        exclude("com.google.inject", "guice")
    }
    implementation("com.beachape:enumeratum_3:1.7.4")
}

tasks {
    compileJava {
        println("javaCompile")
        inputs.sourceFiles.forEach { f ->
            println("\tIn: " + f)
        }
        println()
        outputs.files.forEach { f ->
            println("\tOut: " + f)
        }
        println()
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
        println()
    }
    shadowJar {
//        this.dependsOn(classes)
        System.out.println("inputs: " + inputs.files)
        duplicatesStrategy = DuplicatesStrategy.WARN
        archiveClassifier.set("all")
        exclude("META-INF/versions/11/org/roaringbitmap/ArraysShim.class")
    }
}
//val runeLiteVersion = "1.10.36.1"
//group = "com.fredplugins"
//
//val javaMajorVersion = JavaVersion.VERSION_11.majorVersion
//sourceSets {
//    main {
//        scala {
//            setSrcDirs(listOf(
//                "src/main/ethans",
//                "src/main/lucid",
//                "src/main/plugins",
//            ))
//        }
//    }
//}
//
//dependencies {
//    annotationProcessor(dependencyNotation = "org.projectlombok:lombok:1.18.30")
//    compileOnly("org.projectlombok:lombok:1.18.30")
//    compileOnly("org.pf4j:pf4j:3.10.0")
//    compileOnly("net.runelite:client:$runeLiteVersion")
//
//    implementation("org.scala-lang:scala3-library_3:3.4.2")
////    implementation("org.scala-lang:scala3-staging_3:3.4.2")
//
//    implementation("net.codingwell:scala-guice_3:7.0.0") {
////        exclude("")
//        exclude("com.google.inject", "guice")
//    }
//    // https://mvnrepository.com/artifact/io.github.bishabosha/enum-extensions
////    implementation("io.github.bishabosha:enum-extensions_3:0.1.1")
//    implementation("com.beachape:enumeratum_3:1.7.4")
//    //    implementation("com.lihaoyi:ammonite_3.4.2:3.0.0-M2-15-9bed9700")
//    implementation("org.json:json:20231013") //setTransitive(false) }
//    implementation("org.roaringbitmap:RoaringBitmap:0.9.44")
////    implementation("com.google.archivepatcher:archive-patch-applier:1.0.4")
//    implementation("org.benf:cfr:0.152")
//
////   implementation(files("libs/LucidPlugins-${lucidVersion}.jar"))
//    testImplementation("junit:junit:4.13.1")
//}
//java {
//    toolchain {
//        languageVersion.set(JavaLanguageVersion.of(javaMajorVersion))
//    }
//}
//
//tasks {
//    compileJava {
//        options.encoding = "UTF-8"
//        sourceCompatibility = javaMajorVersion
//        targetCompatibility = javaMajorVersion
//    }
//    compileScala {
//        println("scalaCompile")
//        inputs.sourceFiles.forEach { f ->
//            println("\tIn: " + f)
//        }
//        println()
//        outputs.files.forEach { f ->
//            println("\tOut: " + f)
//        }
//    }
//    jar {
//    }
//    shadowJar {
////        this.dependsOn(classes)
//        System.out.println("inputs: " + inputs.files)
//        duplicatesStrategy = DuplicatesStrategy.WARN
//        archiveClassifier.set("all")
//        exclude("META-INF/versions/11/org/roaringbitmap/ArraysShim.class")
//    }
//}
