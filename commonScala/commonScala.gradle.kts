
description = "Common Scala API"
plugins {
    scala
}
dependencies {
    implementation(project(":common"))
    api("org.scala-lang:scala3-library_3:${Dependencies.scalaVersion}")

    implementation("net.codingwell:scala-guice_3:7.0.0") {
        exclude("com.google.inject", "guice")
    }
    api("com.beachape:enumeratum_3:1.9.2")
    api("org.scala-lang.modules:scala-parser-combinators_3:2.4.0")
//    api("org.wvlet.uni:uni_3:2026.1.0")
    api("com.lihaoyi:fastparse_3:3.1.1")
    api("org.scala-lang.modules:scala-swing_3:3.0.0")

    api("com.lihaoyi:upickle_3:4.4.2")
}

