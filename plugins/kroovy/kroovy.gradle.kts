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
//val fxVersion = "17.0.14"
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