import java.util.Properties

plugins {
    `maven-publish`
    id("java")
}

group = "org.saintqd"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven(url = "https://jitpack.io")
    maven(url = "https://mvn.lumine.io/repository/maven-public/")
    maven(url = "https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven(url = "https://nexus.scarsz.me/content/groups/public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    compileOnly("io.lumine:Mythic-Dist:5.12.2-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("me.clip:placeholderapi:2.11.6") // repo.extendedclip.com
    compileOnly("com.discordsrv:discordsrv:1.30.2")
}

tasks.test {
    useJUnitPlatform()
}
tasks.withType<Jar> {

    // To avoid the duplicate handling strategy error
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // To add all the dependencies otherwise a "NoClassDefFoundError" error
    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })

}
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

publishing {
    publications {
        create<MavenPublication>("maven") {

            groupId = group.toString()
            artifactId = project.name.lowercase()
            version = project.version.toString()

            println("$groupId:$artifactId:$version")

            from(components["java"])
        }
    }

    repositories {
        maven {

            val properties = Properties()
            file("secret.properties").inputStream().use {
                properties.load(it)
            }

            val releasesUrl = uri("https://nexus.vinerium.tech/repository/maven-releases/")
            val snapshotsUrl = uri("https://nexus.vinerium.tech/repository/maven-snapshots/")

            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsUrl else releasesUrl

            credentials.username = properties.getProperty("username")
            credentials.password = properties.getProperty("password")
        }
    }
}