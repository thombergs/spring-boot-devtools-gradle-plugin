plugins {
    `java-gradle-plugin`
    id("org.jetbrains.kotlin.jvm") version "1.3.41"
}

repositories {
    jcenter()
}

gradlePlugin {
    plugins {
        create("simplePlugin") {
            id = "io.reflectoring.devtools"
            implementationClass = "io.reflectoring.devtools.DevToolsPlugin"
        }
    }
}

dependencies {

    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.3.41")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.41")

}