rootProject.name = "FredPlugins"
plugins {
    id("com.gradle.enterprise").version("3.0")
}

include(":ethans")
include(":common")
include(":commonScala")
include(":plugins:recolorCG")
include(":plugins:zulrahHelper")
include(":plugins:attackTimer")
include(":plugins:customPrayers")
include(":plugins:demonicGorilla")
include(":plugins:dt2")
include(":plugins:gauntlet")
include(":plugins:giantsFoundry")
include(":plugins:layoutHelper")
include(":plugins:mixology")
include(":plugins:mta")
include(":plugins:scurriusHelper")
include(":plugins:teleportMaps")
include(":plugins:tempoross")
include(":plugins:titheFarm")
include(":plugins:titheFarm2")
include(":plugins:superClickHelper")

//include(":runelite-api")
//include(":runescape-api")
//include(":runescape-client")
//include(":deobfuscator")
//include(":runelite-script-assembler-plugin")
//include(":runelite-client")
//include(":runelite-mixins")
//include(":injected-client")
//include("injection-annotations")
//include(":runelite-plugin-archetype")
//include(":wiki-scraper")

for (project in rootProject.children) {
    project.apply {
        println(name)
        projectDir = file(name)//(if(file(name).exists()) file(name) else null)//file("plugins/${name}"))
        require(projectDir.isDirectory) { "Project '${project.path} must have a $projectDir directory" }
        buildFileName = "$name.gradle.kts"
        require(buildFile.isFile) { "Project '${project.path} must have a $buildFile build script" }
    }
}
for (project in project(":plugins").children) {
    project.apply {
        println(name)
        projectDir = file("plugins/${name}")//(if(file(name).exists()) file(name) else null)//file("plugins/${name}"))
        require(projectDir.isDirectory) { "Project '${project.path} must have a $projectDir directory" }
        buildFileName = "$name.gradle.kts"
        require(buildFile.isFile) { "Project '${project.path} must have a $buildFile build script" }
    }
}