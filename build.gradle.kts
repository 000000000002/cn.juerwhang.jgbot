import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.shadowJar


plugins {
    kotlin("jvm") version "1.3.41"
    id("com.github.johnrengelman.shadow") version "4.0.4"
    application
}

apply {
    plugin("com.github.johnrengelman.shadow")
    plugin("application")
}

group = "cn.juerwhang"
version = "alpha.2.0"

object Version {
    const val ktor = "1.2.5"
}

application {
    mainClassName = "cn.juerwhang.jgbot.MainBotKt"
}

repositories {
    mavenCentral()
    mavenLocal()
    jcenter()
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.github.JuerGenie:juerobot:alpha.1.8")
    implementation("org.apache.httpcomponents:httpclient:4.5.10")

    implementation("io.ktor:ktor-http:${Version.ktor}")
    implementation("io.ktor:ktor-server-core:${Version.ktor}")
    implementation("io.ktor:ktor-server-netty:${Version.ktor}")
}

//tasks.withType<KotlinCompile> {
//    kotlinOptions.jvmTarget = "9"
//}

tasks {
    named<ShadowJar>("shadowJar") {
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "cn.juerwhang.jgbot.MainBotKt"))
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}
