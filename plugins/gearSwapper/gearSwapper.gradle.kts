description = "Set-up up to 6 custom gear swaps with customizable hotkeys or trigger them via weapon equip"

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


