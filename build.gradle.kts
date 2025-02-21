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
    version = "1.1.0"
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
    implementation(project(":ethans"))
    implementation(project(":common"))
    implementation(project(":commonScala"))

    implementation(project(":plugins:recolorCG"))
    implementation(project(":plugins:zulrahHelper"))
    implementation(project(":plugins:attackTimer"))
    implementation(project(":plugins:customPrayers"))
    implementation(project(":plugins:demonicGorilla"))
    implementation(project(":plugins:dt2"))
    implementation(project(":plugins:gauntlet"))
    implementation(project(":plugins:giantsFoundry"))
    implementation(project(":plugins:layoutHelper"))
    implementation(project(":plugins:mixology"))
    implementation(project(":plugins:mta"))
    implementation(project(":plugins:pvmDebugger"))
    implementation(project(":plugins:pvmHelper"))
    implementation(project(":plugins:scurriusHelper"))
    implementation(project(":plugins:teleportMaps"))
    implementation(project(":plugins:tempoross"))
    implementation(project(":plugins:titheFarm"))
    implementation(project(":plugins:titheFarm2"))
    implementation(project(":plugins:superClickHelper"))
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