plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.3.41"
    `maven-publish`
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