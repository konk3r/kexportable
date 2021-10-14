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
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}

if (JavaVersion.current() >= JavaVersion.VERSION_16) {
    tasks.withType<Test>().configureEach {
        jvmArgs(
            "--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
            "--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
            "--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
            "--add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
            "--add-opens=jdk.compiler/com.sun.tools.javac.jvm=ALL-UNNAMED",
            "--add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
            "--add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
            "--add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
            "--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
            "--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
        )
    }
}
