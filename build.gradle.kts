buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath("org.ajoberstar.grgit:grgit-core:4.1.1")
    }
}

plugins {
    id("java-library")
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("idea")
}

val javaVersion = JavaVersion.VERSION_11

allprojects {
    group = "com.fredplugins"
    version = Dependencies.releaseVersion
    repositories {
        mavenLocal()
        maven(uri("https://repo.runelite.net")) {
            name = "rrn"

            content {
                includeGroupAndSubgroups("net.runelite")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
    if(!name.equals("plugins")) {
        println("allprojects $name")
        apply<JavaLibraryPlugin>()
        apply<IdeaPlugin>()
        apply<MavenPublishPlugin>()

        dependencies {
            this.add("compileOnly", "net.runelite:runelite-api:${Dependencies.rlVersion}")
            this.add("compileOnly", "org.pf4j:pf4j:3.10.0")
            compileOnly("net.runelite:client:${Dependencies.rlVersion}") {
                exclude("net.runelite", "rlicn")
            }
            //           this.add("compileOnly", "net.runelite:client:${Dependencies.rlVersion}")
            this.add("testImplementation", "junit:junit:4.13.1")
            this.add("testImplementation", "org.pf4j:pf4j:3.10.0")
//            this.add("testImplementation", "net.runelite:client:${Dependencies.rlVersion}")
        }
        configure<JavaPluginExtension> {
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
        }
        tasks {
            jar {
                duplicatesStrategy = DuplicatesStrategy.WARN
            }
        }
    }
}

dependencies {
    implementation(projects.ethans)
    implementation(projects.common)
    implementation(projects.commonScala)
    implementation(projects.alchBlocker)
    implementation(projects.attackTimer)
    implementation(projects.demonicGorilla)
    implementation(projects.devkit)
    implementation(projects.dialogAssist)
    implementation(projects.dt2)
    implementation(projects.dynamicHighlights)
    implementation(projects.gauntlet)
    implementation(projects.giantsFoundry)
    implementation(projects.hallowedHelper)
    implementation(projects.layoutHelper)
    implementation(projects.mixology)
    implementation(projects.pvmDebugger)
    implementation(projects.pyramidPlunder)
    implementation(projects.sailingHelper)
    implementation(projects.scurriusHelper)
    implementation(projects.superClickHelper)
    implementation(projects.tempoross)
    implementation(projects.titheFarm2)
    implementation(projects.valeTotems)
    implementation(projects.zulrahHelper)
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

    shadowJar {
        System.out.println("inputs: " + inputs.files)
        duplicatesStrategy = DuplicatesStrategy.WARN
        archiveClassifier.set("all")
        exclude("META-INF/versions/11/org/roaringbitmap/ArraysShim.class")
    }
}