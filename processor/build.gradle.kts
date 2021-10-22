import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion: String by project
val kotlinVersion: String by project
val kotlinpoetVersion: String by project
val googleAutoServiceVersion: String by project

plugins {
    `java-library`
    kotlin("jvm")
    kotlin("kapt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":kexp:annotation-parser"))
    implementation(project(":kexp:kexportable:annotations"))

    implementation("io.ktor:ktor-serialization:$ktorVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("com.squareup:kotlinpoet:$kotlinpoetVersion")
    implementation ("com.squareup:kotlinpoet-classinspector-elements:$kotlinpoetVersion")

    implementation("com.google.auto.service:auto-service:$googleAutoServiceVersion")
    kapt("com.google.auto.service:auto-service:$googleAutoServiceVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.3.0")
    implementation("com.squareup:kotlinpoet:1.7.1")
    implementation("com.squareup:kotlinpoet-metadata:1.9.0")
    implementation("com.squareup:kotlinpoet-metadata-specs:1.9.0")

    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.4.4")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.24")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}
