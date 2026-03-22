description = "Freds Dialog Assistant Plugin"

plugins {
    id("scala")
}

dependencies {
    implementation(project(":ethans"))
    implementation(project(":common"))
    implementation(project(":commonScala"))
}
