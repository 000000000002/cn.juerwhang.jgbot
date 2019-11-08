import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.41"
}

group = "cn.juerwhang"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.github.hydevelop:PicqBotX:4.12.0.991.PRE")
    implementation("org.apache.httpcomponents:httpclient:4.5.10")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "9"
}