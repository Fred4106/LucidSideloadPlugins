import org.gradle.tooling.internal.protocol.ProjectVersion3

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
}

val javaVersion = JavaVersion.VERSION_11

allprojects {
    group = "com.fredplugins"
    version = "1.1.1"
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
    if(!name.equals("plugins")) {
        println("allprojects $name")
        apply<JavaLibraryPlugin>()
        apply<MavenPublishPlugin>()

        dependencies {
            this.add("implementation", "net.runelite:runelite-api:${Dependencies.rlVersion}")
            this.add("compileOnly", "org.pf4j:pf4j:3.10.0")
            this.add("compileOnly", "net.runelite:client:${Dependencies.rlVersion}")
            this.add("testImplementation", "junit:junit:4.13.1")
            this.add("testImplementation", "org.pf4j:pf4j:3.10.0")
            this.add("testImplementation", "net.runelite:client:${Dependencies.rlVersion}")
        }
        configure<JavaPluginExtension> {
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
        }
    }
}

dependencies {
    implementation(projects.ethans)
    implementation(projects.common)
    implementation(projects.commonScala)

    implementation(projects.recolorCG)
    implementation(projects.zulrahHelper)
    implementation(projects.attackTimer)
    implementation(projects.customPrayers)
    implementation(projects.demonicGorilla)
    implementation(projects.dt2)
    implementation(projects.gauntlet)
    implementation(projects.giantsFoundry)
    implementation(projects.kroovy)
    implementation(projects.layoutHelper)
    implementation(projects.mixology)
    implementation(projects.mta)
    implementation(projects.pvmDebugger)
    implementation(projects.pvmHelper)
    implementation(projects.scurriusHelper)
    implementation(projects.teleportMaps)
    implementation(projects.tempoross)
    implementation(projects.titheFarm)
    implementation(projects.titheFarm2)
    implementation(projects.superClickHelper)
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