package io.reflectoring.devtools

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.UnknownConfigurationException

class DevToolsPlugin : Plugin<Project> {


    override fun apply(project: Project) {
        addDevToolsDependency(project)
    }

    /**
     * Adds the dependency to spring-boot-devtools so the user doesn't have to.
     * <p/>
     * The dependency is added to the "developmentOnly" configuration that is also
     * provided by the Spring Boot Gradle plugin, so we re-use that configuration
     * if it exists already.
     */
    private fun addDevToolsDependency(project: Project) {
        val developmentOnlyConfiguration = addOrCreateConfiguration(project, "developmentOnly")
        val springBootPlugin = project.plugins.findPlugin("org.springframework.boot")
        val devToolsDependency = developmentOnlyConfiguration.dependencies.find { it.name == "spring-boot-devtools" }

        // the project already has the dependency and we're not going to overriding it
        if (devToolsDependency != null) {
            return
        }

        // we're adding the dependency without a version to let the Spring Dependency plugin select the version
        if (springBootPlugin != null) {
            project.dependencies.add(developmentOnlyConfiguration.name, "org.springframework.boot:spring-boot-devtools")
            return
        }

        // we're adding the latest version of the dependency
        project.dependencies.add(developmentOnlyConfiguration.name, "org.springframework.boot:spring-boot-devtools:2.3.2.RELEASE")
    }

    private fun addOrCreateConfiguration(project: Project, configurationName: String): Configuration {
        return try {
            project.configurations.getByName(configurationName);
        } catch (e: UnknownConfigurationException) {
            project.configurations.create(configurationName)
        }
    }


}