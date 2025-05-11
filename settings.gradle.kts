import org.gradle.internal.impldep.com.google.api.services.storage.Storage

rootProject.name = "FredPlugins"
plugins {
    id("com.gradle.enterprise").version("3.0")
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":ethans")
include(":common")
include(":commonScala")

//def plugins
val x = listOf("alchBlocker", "recolorCG", "zulrahHelper", "attackTimer", "customPrayers", "demonicGorilla", "dt2", "gearSwapper", "gauntlet", "giantsFoundry", "kroovy", "layoutHelper", "mixology", "mta", "pvmDebugger", "pvmHelper", "scurriusHelper", "teleportMaps", "tempoross", "titheFarm", "titheFarm2", "superClickHelper")
x.forEach {xm->
    include(xm)
}

for (project in rootProject.children) {
//    if(project.name.equals("plugins")) continue;
    project.apply {
        println(name)
        projectDir = file((if(file(name).exists()) name else "plugins/${name}"))
        require(projectDir.isDirectory) { "Project '${project.path} must have a $projectDir directory" }
        buildFileName = "$name.gradle.kts"
        require(buildFile.isFile) { "Project '${project.path} must have a $buildFile build script" }
    }
}
