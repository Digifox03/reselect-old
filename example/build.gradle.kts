plugins {
    id("fabric-loom")
    kotlin("jvm")
}

val modVersion = "0.1-test"
val archiveName = "reselectExample"
val mavenGroup: String by project
val minecraftVersion: String by project
val yarnMappings: String by project
val loaderVersion: String by project
val fabricVersion: String by project
val fabricKotlinVersion: String by project


version = modVersion
group = mavenGroup

base {
    archivesName.set(archiveName)
}

dependencies {
    minecraft("com.mojang", "minecraft", minecraftVersion)
    mappings("net.fabricmc", "yarn", yarnMappings, classifier="v2")
    modImplementation("net.fabricmc", "fabric-loader", loaderVersion)
    implementation(project(":reselect"))
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

tasks.withType<ProcessResources> {
    inputs.property("version", modVersion)
    filesMatching("fabric.mod.json") {
        expand("version" to modVersion)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(16)
}

tasks.withType<Jar> {
    from("../COPYING") {
        rename { "${it}_${this@Build_gradle.archiveName}" }
    }
    from("../COPYING.LESSER") {
        rename { "${it}_${this@Build_gradle.archiveName}" }
    }
}
