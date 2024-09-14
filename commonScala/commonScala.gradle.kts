description = "Common Scala API"
plugins {
    scala
}
dependencies {
    api(project(":common"))

    api("org.scala-lang:scala3-library_3:3.5.0")

    api("net.codingwell:scala-guice_3:7.0.0") {
        exclude("com.google.inject", "guice")
    }
    api("com.beachape:enumeratum_3:1.7.4")

    api("org.scala-lang.modules:scala-swing_3:3.0.0")
}

