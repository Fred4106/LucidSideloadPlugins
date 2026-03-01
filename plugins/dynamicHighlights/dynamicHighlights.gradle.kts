description = "Freds Dynamic Highlighting Plugin"

plugins {
    id("scala")
}

dependencies {
    compileOnly("org.projectlombok:lombok:${Dependencies.lombokVersion}")
    annotationProcessor("org.projectlombok:lombok:${Dependencies.lombokVersion}")
    implementation(project(":ethans"))
    implementation(project(":common"))
    implementation(project(":commonScala"))
}
