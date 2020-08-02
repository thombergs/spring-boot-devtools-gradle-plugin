plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.3.41"
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.10.1"
}

pluginBundle {
    website = "https://github.com/thombergs/spring-boot-devtools-gradle-plugin"
    vcsUrl = "https://github.com/thombergs/spring-boot-devtools-gradle-plugin.git"
    tags = listOf("spring boot", "devtools")
}

group = "io.reflectoring.spring-boot-devtools"
version = "0.0.1-SNAPSHOT"

repositories {
    jcenter()
}

gradlePlugin {
    plugins {
        create("simplePlugin") {
            id = "io.reflectoring.spring-boot-devtools"
            implementationClass = "io.reflectoring.devtools.DevToolsPlugin"
            displayName = "Spring Boot Dev Tools plugin"
            description = "Accelerate the dev loop for your single- or multi-module Spring Boot application."
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.3.41")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.41")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")
    testImplementation("org.assertj:assertj-core:3.16.1")
}

tasks {
    test {
        useJUnitPlatform()
    }
}