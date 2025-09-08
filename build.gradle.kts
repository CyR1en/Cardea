import groovy.json.JsonSlurper
import org.gradle.api.GradleException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

plugins {
    kotlin("jvm") version "2.2.20-RC"
    id("com.gradleup.shadow") version "8.3.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.modrinth.minotaur") version "2.+"
}

group = "com.cyr1en"
version = "1.0.2"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.github.erosb:everit-json-schema:1.14.6")
    implementation("com.h2database:h2:2.3.232")
    implementation("com.zaxxer:HikariCP:5.1.0")
}

tasks {
    runServer {
        minecraftVersion("1.21.8")
    }
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.shadowJar {
    archiveClassifier.set("")

    val shadeBase = "${project.group}.shade"
    relocate("com.zaxxer.hikari", "$shadeBase.hikari")
    relocate("org.h2", "$shadeBase.h2")
    relocate("org.everit.json.schema", "$shadeBase.schema")
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.jar {
    enabled = false
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}

// Only to be used when a new release is ready to be published on GitHub.
// Automated via GitHub Actions.
modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("3C31Qs54")
    versionNumber.set(project.version.toString())
    versionType.set("release")
    uploadFile.set(tasks.shadowJar)
    changelog.set(githubLatestReleaseBody())
    gameVersions.addAll("1.21.8", "1.21.7")
    loaders.add("paper")
    syncBodyFrom.set(rootProject.file("README.md").readText(Charsets.UTF_8))
}

tasks.modrinth {
    dependsOn(tasks.modrinthSyncBody)
}

fun githubLatestReleaseBody(): String {
    val url = "https://api.github.com/repos/cyr1en/cardea/releases/latest"
    val client = HttpClient.newHttpClient()
    val request =
        HttpRequest
            .newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "gradle-build-script")
            .GET()
            .build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    if (response.statusCode() / 100 != 2) {
        throw GradleException("Failed to fetch latest release: HTTP ${response.statusCode()} - ${response.body()}")
    }
    val json = JsonSlurper().parseText(response.body()) as Map<*, *>
    return (json["body"] as String?) ?: ""
}
