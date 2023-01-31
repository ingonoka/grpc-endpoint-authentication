/*
 * Copyright (c) 2023. Ingo Noka
 * This file belongs to project grpc-endpoint-authentication.
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */
@file:Suppress("UnstableApiUsage")

import java.io.ByteArrayOutputStream

plugins {
    kotlin("multiplatform") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"
    id("com.android.library") version "7.3.0"
    id("maven-publish")
    id("signing")
}

repositories {
    google()
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    android {
        publishLibraryVariants("release")
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.4.1")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("org.slf4j:slf4j-api:1.7.36")
                implementation("com.google.protobuf:protobuf-java:3.21.12")
                implementation("io.grpc:grpc-protobuf:1.52.1")
            }
        }
        val jvmTest by getting

        val androidMain by getting {
            dependencies {
                implementation("com.google.protobuf:protobuf-javalite:3.21.12")
                implementation("io.grpc:grpc-protobuf-lite:1.52.1")
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
            }
        }
    }
}

android {
    namespace = "com.ingonoka.grpcendpointauthentication"
    compileSdk = 33
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 24
        targetSdk = 33
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    publishing {
        singleVariant("release")
    }
}

fun getVersionCode() = try {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine = listOf("git", "rev-list", "--first-parent", "--count", "master")
        standardOutput = stdout
        errorOutput = ByteArrayOutputStream()
    }
    Integer.parseInt(stdout.toString().trim())
} catch (e: Exception) {
    0
}

fun getVersionName() = try {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine = listOf("git", "describe", "--tags") //, '--long'
        standardOutput = stdout
        errorOutput = ByteArrayOutputStream()
    }
    stdout.toString().trim()
} catch (e: Exception) {
    "v0.0"
}

// See configuration on ~/.gradle/gradle.properties
signing {
    sign(publishing.publications)
}

// Create empty javadoc to satisfy validation for maven central via sonatype staging
val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

afterEvaluate {

    publishing {

        publications {

            withType<MavenPublication> {
                artifact(javadocJar.get())
                pom {
                    name.set("Endpoint authentication in grpc calls")
                    description.set("A library implementing a simple protocol to identify and authenticate endpoints in grpc calls")
                    url.set("https://github.com/ingonoka/grpc-endpoint-authentication")

                    issueManagement {
                        system.set("Github")
                        url.set("https://github.com/ingonoka/grpc-endpoint-authentication/issues")
                    }

                    licenses {
                        license {
                            name.set("Attribution-NonCommercial-NoDerivatives 4.0 International (CC BY-NC-ND 4.0)")
                            url.set("https://creativecommons.org/licenses/by-nc-nd/4.0/")
                        }
                    }
                    scm {
                        connection.set("scm:git:git@github.com:ingonoka/grpc-endpoint-authentication")
                        developerConnection.set("scm:git:git@github.com:ingonoka/grpc-endpoint-authentication")
                        url.set("https://github.com/ingonoka/grpc-endpoint-authentication")
                    }
                    developers { developer { name.set("Ingo Noka") } }
                }
            }

            repositories {

                maven {
                    name = "Sonatype"
                    url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                    credentials {
                        username = providers.gradleProperty("sonatype_uid").get()
                        password = providers.gradleProperty("sonatype_pw").get()
                    }
                }
            }


            project.getTasksByName("publishJvmPublicationToSonatypeRepository", false)
                .first()
                .dependsOn("signJvmPublication", "signAndroidReleasePublication", "signKotlinMultiplatformPublication")
            project.getTasksByName("publishAndroidReleasePublicationToSonatypeRepository", false)
                .first()
                .dependsOn("signJvmPublication", "signAndroidReleasePublication", "signKotlinMultiplatformPublication")
            project.getTasksByName("publishKotlinMultiplatformPublicationToSonatypeRepository", false)
                .first()
                .dependsOn("signJvmPublication", "signAndroidReleasePublication", "signKotlinMultiplatformPublication")

        }
    }
}

group = "com.ingonoka"
version = getVersionName()