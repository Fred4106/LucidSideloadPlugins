description = "Script Master"

plugins {
    id("scala")
}

dependencies {

    annotationProcessor("org.projectlombok:lombok:${Dependencies.lombokVersion}")
    compileOnly("org.projectlombok:lombok:${Dependencies.lombokVersion}")

    implementation(project(":ethans"))
    implementation(project(":common"))
    implementation(project(":commonScala"))
    api("org.scala-lang:scala3-compiler_3:${Dependencies.scalaVersion}")
}


