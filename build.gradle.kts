import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.41"
}

group = "cn.juerwhang"
version = "alpha.1.3"

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

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "9"
}