description = "Ethans API"

dependencies {
    implementation(project(":common"))
    implementation(project(":commonScala"))

    annotationProcessor("org.projectlombok:lombok:${Dependencies.lombokVersion}")
    compileOnly("org.projectlombok:lombok:${Dependencies.lombokVersion}")
    annotationProcessor("org.jetbrains:annotations:${Dependencies.jetbrainsAnnotations}")
    api("org.jetbrains:annotations:${Dependencies.jetbrainsAnnotations}")

    implementation("org.json:json:20231013")
    implementation("org.roaringbitmap:RoaringBitmap:0.9.44")
    implementation("org.benf:cfr:0.152")
}

