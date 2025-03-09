description = "Gauntlet Plugin"

dependencies {
    annotationProcessor("org.projectlombok:lombok:${Dependencies.lombokVersion}")
    compileOnly("org.projectlombok:lombok:${Dependencies.lombokVersion}")
    implementation(projects.ethans)
    implementation(projects.common)
    implementation(projects.attackTimer)
}


