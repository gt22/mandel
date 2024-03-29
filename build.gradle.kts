import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.41"
    id("com.github.johnrengelman.shadow") version "4.0.4"
}

group = "com.gt22"
version = "1.0"

repositories {
    maven("http://npm.mipt.ru:8081/artifactory/gradle-dev")
    maven("https://jitpack.io")
    mavenCentral()
}



val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "mandel.MainKt"
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("no.tornado:tornadofx:1.7.17")
    implementation("scientifik:kmath-core-jvm:0.1.0-dev")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}