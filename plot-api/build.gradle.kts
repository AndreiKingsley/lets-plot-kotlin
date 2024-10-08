/*
 * Copyright (c) 2021. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

plugins {
    kotlin("multiplatform")
    `maven-publish`
    signing
    // Add the KSP plugin before the Jupyter API to avoid ksp versions incompatibility.
    // May be removed when using further versions of the jupyter api
    id("com.google.devtools.ksp")
    kotlin("jupyter.api") version "0.12.0-308"
}

val letsPlotVersion = extra["letsPlot.version"] as String
val datetimeVersion = extra["datetime.version"] as String
val kotlinLoggingVersion = extra["kotlinLogging.version"] as String
val assertjVersion = extra["assertj.version"] as String

kotlin {
    jvm()
    js().browser()

    sourceSets {
        commonMain {
            dependencies {
                api("org.jetbrains.lets-plot:commons:$letsPlotVersion")
                api("org.jetbrains.lets-plot:datamodel:$letsPlotVersion")
                api("org.jetbrains.lets-plot:plot-base:$letsPlotVersion")
                api("org.jetbrains.lets-plot:plot-builder:$letsPlotVersion")
                api("org.jetbrains.lets-plot:plot-stem:$letsPlotVersion")

                api("org.jetbrains.lets-plot:deprecated-in-v4:$letsPlotVersion")
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$datetimeVersion")
            }
        }

        named("jvmMain") {
            dependencies {
                implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")
                api("org.jetbrains.lets-plot:lets-plot-common:$letsPlotVersion")
                // Use "-jvm" variant to work around the issue where LPK JS (IR) artefact becomes dependent on
                // the "kotlinx-datetime".
                // See also:
                // https://youtrack.jetbrains.com/issue/KT-52812/JSIR-compiler-error-Could-not-find-orgjetbrainskotlinxkotlinx-datetime-in-USERLibraryApplication-Supportkotlindaemon
                compileOnly("org.jetbrains.kotlinx:kotlinx-datetime-jvm:$datetimeVersion")

                compileOnly("org.jetbrains.lets-plot:lets-plot-batik:$letsPlotVersion")
                compileOnly("org.jetbrains.lets-plot:lets-plot-jfx:$letsPlotVersion")
                compileOnly("org.jetbrains.lets-plot:lets-plot-image-export:$letsPlotVersion")
            }
        }

        named("jsMain") {
            dependencies {
                implementation("io.github.microutils:kotlin-logging-js:$kotlinLoggingVersion")
            }
        }

        jvmTest {
            dependencies {
                // assertj
                implementation("org.assertj:assertj-core:$assertjVersion")

                implementation("org.jetbrains.lets-plot:lets-plot-image-export:$letsPlotVersion")
            }
        }
    }
}


val artifactBaseName = "lets-plot-kotlin"
val artifactGroupId = project.group as String
val artifactVersion = project.version as String

val jarJavaDocs by tasks.creating(Jar::class) {
    archiveClassifier.set("javadoc")
    group = "lets plot"
    from("$rootDir/README.md")
}

afterEvaluate {
    publishing {
        publications {
            // Build artifact "lets-plot-kotlin-kernel" with no dependencies in POM.
            create<MavenPublication>("letsPlotKotlinKernel") {
                artifactId = "$artifactBaseName-kernel"

                val jvmJar: Task by tasks
                val jvmSourcesJar: Task by tasks
                artifact(jvmJar)
                artifact(jvmSourcesJar)

                pom {
                    name.set("Lets-Plot Kotlin API (for Jupyter Kotlin Kernel)")
                    description.set("Lets-Plot Kotlin API without dependencies.")
                }
            }

        }

        publications.forEach {
            with(it as MavenPublication) {
                groupId = artifactGroupId
                version = artifactVersion

                if (!artifactId.startsWith(artifactBaseName)) {
                    // Default multiplatform artifacts: rename.
                    artifactId = artifactId.replace(project.name, artifactBaseName)
                    pom {
                        name.set("Lets-Plot Kotlin API")
                        description.set("Lets-Plot Kotlin API.")
                    }
                }

                // Add "javadocs" to each publication or Maven won't publish it.
                artifact(jarJavaDocs)

                pom {
                    url.set("https://github.com/JetBrains/lets-plot-kotlin")
                    licenses {
                        license {
                            name.set("MIT")
                            url.set("https://raw.githubusercontent.com/JetBrains/lets-plot-kotlin/master/LICENSE")
                        }
                    }
                    developers {
                        developer {
                            id.set("jetbrains")
                            name.set("JetBrains")
                            email.set("lets-plot@jetbrains.com")
                        }
                    }
                    scm {
                        url.set("https://github.com/JetBrains/lets-plot-kotlin")
                    }
                }

                // Sign all publications.
                if (!(project.version as String).contains("SNAPSHOT")) {
                    signing.sign(it)
                }
            }
        }

        repositories {
            mavenLocal {
                val localMavenRepository: String by project
                url = uri(localMavenRepository)
            }
        }
    }
}


tasks {
    // Store versions in properties to later access at runtime.
    val saveVersions by creating {
        doLast {
            File("${projectDir}/src/jvmMain/resources/letsPlotKotlinAPI/", "version.properties").writeText(
                """
                lets_plot.version=$letsPlotVersion
                lets_plot_kotlin_api.version=${project.version}
                """.trimIndent()
            )

        }
    }

//compileKotlin.dependsOn += saveVersions
    val jvmMainClasses by getting {
        dependsOn += saveVersions
    }
}

tasks.processJupyterApiResources {
    libraryProducers = listOf("org.jetbrains.letsPlot.jupyter.Integration")
}

//task printIt {
//    print("${project.name}: ${uri(project.localMavenRepository)}")
//}

