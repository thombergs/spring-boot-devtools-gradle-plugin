plugins {
    `java-gradle-plugin`
    id("org.jetbrains.kotlin.jvm") version "1.3.41"
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

}