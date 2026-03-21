import org.gradle.internal.impldep.com.google.api.services.storage.Storage

rootProject.name = "FredPlugins"
plugins {
    id("com.gradle.enterprise").version("3.0")
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":ethans")
include(":common")
include(":commonScala")

val x = listOf(
    "alchBlocker",
    "attackTimer",
    "customPrayers",
    "demonicGorilla",
    "devkit",
    "dialogAssist",
    "dt2",
    "dynamicHighlights",
    "gauntlet",
    "gearSwapper",
    "giantsFoundry",
    "hallowedHelper",
    "layoutHelper",
    "mixology",
    "pvmDebugger",
    "pyramidPlunder",
    "recolorCG",
    "sailingHelper",
    "scurriusHelper",
    "superClickHelper",
    "teleportMaps",
    "tempoross",
    "titheFarm2",
    "valeTotems",
    "zulrahHelper"
)
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
