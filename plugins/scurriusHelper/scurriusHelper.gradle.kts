description = "Scurrius Helper Plugin"

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
}


