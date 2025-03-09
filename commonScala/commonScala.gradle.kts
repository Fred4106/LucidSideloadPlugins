
description = "Common Scala API"
plugins {
    scala
}
dependencies {
    implementation(project(":ethans"))
    implementation(project(":common"))
    api("org.scala-lang:scala3-library_3:${Dependencies.scalaVersion}")

    api("net.codingwell:scala-guice_3:7.0.0") {
        exclude("com.google.inject", "guice")
    }
    api("com.beachape:enumeratum_3:1.7.4")

    api("org.scala-lang.modules:scala-swing_3:3.0.0")
}

