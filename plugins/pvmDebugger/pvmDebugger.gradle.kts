description = "Pvm Debugger Plugin"

plugins {
    id("scala")
}

dependencies {
    annotationProcessor("org.projectlombok:lombok:${Dependencies.lombokVersion}")
    compileOnly("org.projectlombok:lombok:${Dependencies.lombokVersion}")

    implementation("com.lihaoyi:upickle_3:3.1.0")
    implementation(project(":ethans"))
    implementation(project(":common"))
    implementation(project(":commonScala"))
    implementation(project(":attackTimer"))
    implementation("org.scala-lang.modules:scala-swing_3:3.0.0")
    implementation("org.scala-lang.modules:scala-parser-combinators_3:2.4.0")
}
