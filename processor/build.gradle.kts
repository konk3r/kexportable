import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File
import java.io.FileInputStream
import java.util.*

val ktorVersion: String by project
val assertKVersion: String by project
val kotlinVersion: String by project
val kotlinpoetVersion: String by project
val googleAutoServiceVersion: String by project

plugins {
    `java-library`
    `maven-publish`
    signing
    kotlin("jvm")
    kotlin("kapt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.casadetasha:annotation-parser:0.1.0")
    implementation(project(":annotations"))

    implementation("io.ktor:ktor-serialization:$ktorVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("com.squareup:kotlinpoet:$kotlinpoetVersion")
    implementation("com.squareup:kotlinpoet-metadata:$kotlinpoetVersion")

    implementation("com.google.auto.service:auto-service:$googleAutoServiceVersion")
    kapt("com.google.auto.service:auto-service:$googleAutoServiceVersion")

    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.4.9")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:$assertKVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}

val prop = Properties().apply {
    load(FileInputStream(File(project.gradle.gradleUserHomeDir, "local.properties")))
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    dependsOn("javadoc")
    from(tasks.javadoc.get().destinationDir)
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    repositories {
        maven {
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = prop.getProperty("ossrhUsername")
                password = prop.getProperty("ossrhPassword")
            }
        }
    }

    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])

            group = "com.casadetasha"
            artifactId = "kexportable-processor"
            version = "0.1.0"

            artifact(sourcesJar.get())
            artifact(javadocJar.get())

            pom {
                name.set("Kexportable")
                description.set("KAPT processor to add an export() method to output a serializable POKO of the class." +
                        " To be used in conjunction with kexportable.")
                url.set("https://github.com/konk3r/kexportable")

                scm {
                    connection.set("scm:git://github.com/konk3r/kexportable")
                    developerConnection.set("scm:git:git@github.com:konk3r/kexportable.git")
                    url.set("https://github.com/konk3r/kexportable")
                }

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("gabriel")
                        name.set("Gabriel Spencer")
                        email.set("gabriel@casadetasha.dev")
                    }
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}
