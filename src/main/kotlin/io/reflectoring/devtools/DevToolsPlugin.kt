package io.reflectoring.devtools

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.UnknownConfigurationException

class DevToolsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val devConfiguration = addDevelopmentOnlyConfiguration(project)
        addDevToolsDependency(project, devConfiguration)
    }

    /**
     * Adds the dependency to spring-boot-devtools so the user doesn't have to.
     */
    private fun addDevToolsDependency(project: Project, configuration: Configuration) {
        val springBootPlugin = project.plugins.findPlugin("org.springframework.boot")
        val devToolsDependency = configuration.dependencies.find { it.name == "spring-boot-devtools" }

        // the project already has the dependency and we're not going to overriding it
        if (devToolsDependency != null) {
            return
        }

        // we're adding the dependency without a version to let the Spring Dependency plugin select the version
        if (springBootPlugin != null) {
            project.dependencies.add(configuration.name, "org.springframework.boot:spring-boot-devtools")
            return
        }

        // we're adding the latest version of the dependency
        project.dependencies.add(configuration.name, "org.springframework.boot:spring-boot-devtools:2.3.2.RELEASE")
    }

    /**
     * Creates the "developmentOnly" configuration if it doesn't already exists. Returns the existing
     * configuration, if it already exists.
     */
    private fun addDevelopmentOnlyConfiguration(project: Project): Configuration {
        return try {
            project.configurations.getByName("developmentOnly");
        } catch (e: UnknownConfigurationException) {
            project.configurations.create("developmentOnly")
        }
    }


}