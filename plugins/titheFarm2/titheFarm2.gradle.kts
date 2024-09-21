description = "Tithe Farm V2 Plugin"

plugins {
    id("scala")
}

dependencies {
    annotationProcessor("org.projectlombok:lombok:${Dependencies.lombokVersion}")
    compileOnly("org.projectlombok:lombok:${Dependencies.lombokVersion}")
    implementation(project(":ethans"))
    implementation(project(":commonScala"))
}



