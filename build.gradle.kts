import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.6.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

group = "org.fever"
version = readFile("VERSION")

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    // testImplementation("junit:junit:4.12")

    intellijPlatform {
        val version = providers.gradleProperty("platformVersion")
        create(IntelliJPlatformType.PyCharmProfessional, version)

        bundledPlugin("org.jetbrains.plugins.yaml")
        // https://plugins.jetbrains.com/docs/intellij/pycharm.html#python-plugins
        bundledPlugin("PythonCore") // For PyCharm Community
        bundledPlugin("Pythonid") // For PyCharm Professional
    }
}

intellijPlatform {
    pluginConfiguration {
        id = "org.fever.pypendency"
        name = "Pypendency"
        changeNotes = readFile("whats-new.txt").replace("{{version}}", project.version.toString())

        ideaVersion {
            sinceBuild = "231"
            untilBuild = provider { null }
        }
    }
}

fun readFile(name: String): String {
    return file(name).readText().trim()
}
