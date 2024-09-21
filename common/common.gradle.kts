description = "Common API"
dependencies {
    annotationProcessor("org.projectlombok:lombok:${Dependencies.lombokVersion}")
    compileOnly("org.projectlombok:lombok:${Dependencies.lombokVersion}")
    implementation(project(":ethans"))
//    api(project(":lucid"))

//    implementation("org.json:json:20231013")
//    implementation("org.roaringbitmap:RoaringBitmap:0.9.44")
//    implementation("org.benf:cfr:0.152")

//    api("org.scala-lang:scala3-library_3:3.4.2")

//    api("net.codingwell:scala-guice_3:7.0.0") {
  //      exclude("com.google.inject", "guice")
  //  }
    //api("com.beachape:enumeratum_3:1.7.4")
}

