description = "Kroovy Plugin"

plugins {
    java
	groovy
    id("scala")
}

var fxOsName = System.getProperty("os.name")
if(fxOsName.startsWith("Linux"))
    fxOsName = "linux"
else if(fxOsName.startsWith("Mac"))
    fxOsName = "mac"
else if(fxOsName.startsWith("Windows"))
    fxOsName = "win"
else {
    throw Exception("Unknown platform!")
}

val fxVersion = "17.0.14"

val fxModules = setOf("base", "controls", "fxml", "graphics", "media", "swing", "web")
val osNames = setOf("linux", "mac", "win")

dependencies {
    annotationProcessor("org.projectlombok:lombok:${Dependencies.lombokVersion}")
    compileOnly("org.projectlombok:lombok:${Dependencies.lombokVersion}")
    annotationProcessor("org.jetbrains:annotations:${Dependencies.jetbrainsAnnotations}")
    compileOnly("org.jetbrains:annotations:${Dependencies.jetbrainsAnnotations}")
    implementation(project(":ethans"))
    implementation(project(":common"))
    implementation(project(":commonScala"))
    implementation("org.scala-lang.modules:scala-collection-contrib_3:0.4.0")
    implementation("org.fxmisc.richtext:richtextfx:0.10.5")
    implementation(group = "com.lihaoyi", name = "fastparse_3", version = "3.1.1")
    implementation(group = "org.antlr", name = "antlr4-runtime", version = "4.8-1")
    implementation(group = "org.apache.groovy", name="groovy", version="4.0.0-alpha-1")
    implementation(group = "com.typesafe.play", name = "play-json_3", version = "2.10.6")
    fxModules.forEach { k ->
        implementation(group = "org.openjfx", name = "javafx-$k", version = "14.0.1")
        implementation(group = "org.openjfx", name = "javafx-$k", version = "14.0.1", classifier = "win")
    }

    implementation(group = "org.scalaz", name = "scalaz-core_3", version = "7.4.0-M15")

    tasks {
		val compileJava = named("compileJava", JavaCompile::class).get()
		compileJava.enabled = false

		val compileGroovy = named("compileGroovy", GroovyCompile::class).get()
		val compileScala = named("compileScala", ScalaCompile::class).get()
//		val compileKotlin = named("compileKotlin", KotlinCompile::class).get()
		val classes by getting

		compileGroovy.classpath = sourceSets.main.get().compileClasspath

		compileScala.classpath = sourceSets.main.get().compileClasspath
		compileScala.classpath += files(compileGroovy.destinationDirectory)
 		compileScala.setDependsOn(mutableListOf(compileGroovy))
	}
}