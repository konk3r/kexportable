import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val assertKVersion: String by project
val ktorVersion: String by project
val kotlinVersion: String by project

plugins {
    application
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":annotations"))
    kapt(project(":processor"))

    implementation("io.ktor:ktor-serialization:$ktorVersion")

    testImplementation("com.willowtreeapps.assertk:assertk-jvm:$assertKVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}
