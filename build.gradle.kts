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
version = "alpha.1.3"

application {
    mainClassName = "cn.juerwhang.jgbot.MainBotKt"
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.github.hydevelop:PicqBotX:4.12.0.991.PRE")
    implementation("org.apache.httpcomponents:httpclient:4.5.10")

    implementation("me.liuwj.ktorm:ktorm-core:2.6")
    implementation("me.liuwj.ktorm:ktorm-support-sqlite:2.6")
    implementation("org.xerial:sqlite-jdbc:3.28.0")
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
