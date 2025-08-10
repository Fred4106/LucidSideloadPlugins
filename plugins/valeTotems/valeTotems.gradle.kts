description = "Vale Totems Plugin"

plugins {
    id("scala")
}

dependencies {
    annotationProcessor("org.projectlombok:lombok:${Dependencies.lombokVersion}")
    compileOnly("org.projectlombok:lombok:${Dependencies.lombokVersion}")
    implementation(project(":ethans"))
    implementation(project(":common"))
    implementation(project(":commonScala"))
}


