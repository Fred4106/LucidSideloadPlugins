rootProject.name = "FredPlugins"
plugins {
    id("com.gradle.enterprise").version("3.0")
}

include(":ethans")
include(":common")
include(":commonScala")
include(":attackTimer")
include(":customPrayers")
include(":demonicGorilla")
include(":dt2")
include(":gauntlet")
include(":giantsFoundry")
include(":layoutHelper")
include(":mixology")
include(":mta")
include(":scurriusHelper")
include(":tempoross")
include(":titheFarm")
include(":titheFarm2")
include(":superClickHelper")

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
        projectDir = (if(file(name).exists()) file(name) else file("plugins/${name}"))
        buildFileName = "$name.gradle.kts"
        require(projectDir.isDirectory) { "Project '${project.path} must have a $projectDir directory" }
        require(buildFile.isFile) { "Project '${project.path} must have a $buildFile build script" }
    }
}