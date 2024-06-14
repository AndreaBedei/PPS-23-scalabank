/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Scala application project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.3/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
    // Apply the scala Plugin to add support for Scala.
    scala

    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    implementation("org.scala-lang:scala3-compiler_3:3.4.0")

    // This dependency is used by the application.
    implementation("com.google.guava:guava:32.1.1-jre")
    implementation("com.softwaremill.sttp.client3:core_2.13:3.8.3")
    implementation("com.softwaremill.sttp.client3:async-http-client-backend-future_2.13:3.8.3")
    implementation("io.circe:circe-core_2.13:0.14.1")
    implementation("io.circe:circe-generic_2.13:0.14.1")
    implementation("io.circe:circe-parser_2.13:0.14.1")

    // Use Scalatest for testing our library
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.scalatest:scalatest_3:3.2.10")
    testImplementation("org.scalatestplus:junit-4-13_3:3.2.10.0")

    testRuntimeOnly("org.scala-lang.modules:scala-xml_3:2.0.1")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    // Define the main class for the application.
    mainClass.set("scalabank.App")
}
