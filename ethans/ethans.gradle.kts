description = "Ethans API"

dependencies {
    annotationProcessor("org.projectlombok:lombok:${Dependencies.lombokVersion}")
    compileOnly("org.projectlombok:lombok:${Dependencies.lombokVersion}")
    annotationProcessor("org.jetbrains:annotations:${Dependencies.jetbrainsAnnotations}")
    compileOnly("org.jetbrains:annotations:${Dependencies.jetbrainsAnnotations}")
//    annotationProcessor(dependencyNotation = "org.projectlombok:lombok:1.18.30")
//    compileOnly("org.projectlombok:lombok:1.18.30")
//    compileOnly("org.pf4j:pf4j:3.10.0")
//    compileOnly("net.runelite:client:$runeLiteVersion")

//    implementation("org.scala-lang:scala3-library_3:3.4.2")
//    implementation("org.scala-lang:scala3-staging_3:3.4.2")
//    implementation("net.codingwell:scala-guice_3:7.0.0") {
//        exclude("com.google.inject", "guice")
//    }
//    implementation("com.beachape:enumeratum_3:1.7.4")
    //    implementation("com.lihaoyi:ammonite_3.4.2:3.0.0-M2-15-9bed9700")
//    implementation("com.google.archivepatcher:archive-patch-applier:1.0.4")
//   implementation(files("libs/LucidPlugins-${lucidVersion}.jar"))

    implementation("org.json:json:20231013") //setTransitive(false) }
    implementation("org.roaringbitmap:RoaringBitmap:0.9.44")
    implementation("org.benf:cfr:0.152")

//    testImplementation("junit:junit:4.13.1")
}

