description = "Kroovy Plugin"

plugins {
    id("scala")
}

//var fxOsName = System.getProperty("os.name")
//if(fxOsName.startsWith("Linux"))
//    fxOsName = "linux"
//else if(fxOsName.startsWith("Mac"))
//    fxOsName = "mac"
//else if(fxOsName.startsWith("Windows"))
//    fxOsName = "win"
//else {
//    throw Exception("Unknown platform!")
//}
//
val fxVersion = "17.0.14"
//
//val fxModules = setOf("base", "controls", "fxml", "graphics", "media", "swing", "web")
//val osNames = setOf("linux", "mac", "win")

dependencies {
    annotationProcessor("org.projectlombok:lombok:${Dependencies.lombokVersion}")
    compileOnly("org.projectlombok:lombok:${Dependencies.lombokVersion}")
    annotationProcessor("org.jetbrains:annotations:${Dependencies.jetbrainsAnnotations}")
    compileOnly("org.jetbrains:annotations:${Dependencies.jetbrainsAnnotations}")
    implementation(project(":ethans"))
    implementation(project(":common"))
    implementation(project(":commonScala"))
//    implementation(group = "org.openjfx", name = "javafx-base", version = fxVersion, classifier = "win")
//    implementation ("dev.zio:izumi-reflect_3:3.0.2")
//    implementation("co.blocke:scala-reflection_3:2.0.11")
    implementation("io.github.gaeljw:typetrees_3:0.5.0")
    implementation("io.bullet:spliff_3:0.8.0")
//    testImplementation("org.pf4j:pf4j:3.10.0")
//    testImplementation("org.slf4j:slf4j-api:2.0.6")
    testImplementation("ch.qos.logback:logback-classic:1.4.12")
//    testImplementation("ch.qos.logback:logback-core:1.2.9")
//    fxModules.forEach { k ->
////        implementation(group = "org.openjfx", name = "javafx-$k", version = "14.0.1")
//        compileOnly(group = "org.openjfx", name = "javafx-$k", version = fxVersion, classifier = "win")
//    }
//
//    implementation("org.scalafx:scalafx_3:17.0.1-R26")
}
tasks {
    processResources {
        inputs.sourceFiles.forEach { f ->
            println("\tIn: " + f)
        }
    }
    jar {
        duplicatesStrategy = DuplicatesStrategy.WARN
    }
}