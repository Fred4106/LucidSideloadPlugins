import org.gradle.internal.impldep.com.google.api.services.storage.Storage

rootProject.name = "FredPlugins"
plugins {
    id("com.gradle.enterprise").version("3.0")
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":ethans")
include(":common")
include(":commonScala")
//"customPrayers"
//"mta"
//"pvmHelper"
//"titheFarm"
//"kroovy"

val x = listOf("customPrayers", "alchBlocker", "dialogAssist", "gearSwapper", "recolorCG", "scriptMaster", "pyramidPlunder", "zulrahHelper", "attackTimer", "demonicGorilla", "devkit", "dt2",  "gauntlet", "giantsFoundry",  "hallowedHelper", "layoutHelper", "mixology", "pvmDebugger", "sailingHelper", "scurriusHelper", "teleportMaps", "tempoross", "titheFarm2", "superClickHelper", "valeTotems")
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
