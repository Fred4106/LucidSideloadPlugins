description = "Kroovy Plugin"

plugins {
    id("scala")
}


dependencies {
    annotationProcessor("org.projectlombok:lombok:${Dependencies.lombokVersion}")
    compileOnly("org.projectlombok:lombok:${Dependencies.lombokVersion}")
    annotationProcessor("org.jetbrains:annotations:${Dependencies.jetbrainsAnnotations}")
    compileOnly("org.jetbrains:annotations:${Dependencies.jetbrainsAnnotations}")
    implementation(project(":ethans"))
    implementation(project(":common"))
    implementation(project(":commonScala"))
    implementation("org.scala-lang.modules:scala-collection-contrib_3:0.4.0")
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
