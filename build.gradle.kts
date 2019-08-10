
import com.google.protobuf.gradle.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.intellij") version "0.4.9"
    id("com.google.protobuf") version "0.8.10"
    kotlin("jvm") version "1.3.40"
}

group = "io.project"
version = "1.0-SNAPSHOT"

project.extensions.add("protobuf", ProtobufConfigurator(project, null))

repositories {
    google()
    mavenCentral()
    jcenter()
    maven("https://plugins.gradle.org/m2/")

    flatDir {
        dirs(
            "${rootDir}/../kroto-plus/kroto-plus-message/build/libs/",
            "${rootDir}/../kroto-plus/kroto-plus-coroutines/build/libs/",
            "${rootDir}/../kroto-plus/kroto-plus-test/build/libs/",
            "${rootDir}/../kroto-plus/protoc-gen-kroto-plus/build/libs/"
        )
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.google.protobuf:protobuf-java:3.9.0")
//    implementation("com.google.protobuf:protobuf-gradle-plugin:0.8.10")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}


val wrapper = tasks.getByName<Wrapper>("wrapper") {
    gradleVersion = "5.5"
    distributionType = Wrapper.DistributionType.ALL
}


tasks.withType<KotlinCompile> {
    dependsOn.add("generateProto")
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

configure<ProtobufConfigurator> {
    generatedFilesBaseDir = "$projectDir/src/"


    protoc {
        artifact = "com.google.protobuf:protoc:3.9.0"
    }

    plugins {
        id("javalite") {
            artifact = "com.google.protobuf:protoc-gen-javalite:3.0.0"
        }
        id("krotoPlus") {
            artifact = "com.github.marcoferrer.krotoplus:protoc-gen-kroto-plus:+:jvm8@jar"
        }
    }

    generateProtoTasks {
        val krotoConfig = file("$projectDir/krotoPlusConfig.yaml")

        all().forEach {
            it.inputs.files.plus(krotoConfig)

            it.builtins {
                remove("java")
            }
            it.plugins {
                id("javalite") {
                    outputSubDir = "java"
                }

                id("krotoPlus") {
                    println("kroto")
                    outputSubDir = "java"
                    option("ConfigPath=$projectDir/krotoPlusConfig.yaml")
                }
            }
        }
    }
}
