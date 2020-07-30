package io.reflectoring.devtools

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.artifacts.UnknownConfigurationException
import org.gradle.api.tasks.Copy
import java.io.File

class DevToolsPlugin : Plugin<Project> {


    override fun apply(project: Project) {
        addDevToolsDependency(project)
        val reloadConfiguration = addOrCreateConfiguration(project, "reload")

        project.afterEvaluate {
            val tasks = mutableListOf<Task>()
            reloadConfiguration.resolve()
            reloadConfiguration.resolvedConfiguration.firstLevelModuleDependencies.forEach { dependency ->
                tasks.add(createReloadClassesTaskForModule(project, dependency))
                tasks.add(createReloadResourcesTaskForModule(project, dependency))
            }
            createAggregateReloadTask(project, tasks)
        }

    }

    private fun createAggregateReloadTask(project: Project, tasks: List<Task>) {
        val reloadTask = project.tasks.create("reload")
        for (task in tasks) {
            reloadTask.dependsOn(task)
        }
    }

    /**
     * Creates a task that compiles the Java files in a given module and then copies them into
     * the build folder of the main module for Spring Boot Dev Tools to pick up for reload.
     */
    private fun createReloadClassesTaskForModule(project: Project, module: ResolvedDependency): Task {
        val reloadClassesTask = project.tasks.create("reloadClassesFrom${module.moduleName}", Copy::class.java)
        module.moduleArtifacts.forEach {
            if (it.type == "jar") {
                // A lot of assumptions here:
                // - we assume a single JAR dependency per module
                // - we assume the JAR file is in the build/libs folder
                // - we assume classes of the module have been compiled to build/classes
                // - we assume that Spring Boot dev tools is watching the build/classes folder of this project
                val classesFolder = File(it.file.parentFile.parentFile, "classes").absolutePath
                reloadClassesTask.from(classesFolder)
                reloadClassesTask.into("build/classes")
            }
        }

        // we assume that the module and the main module have the "java" plugin applied
        reloadClassesTask.dependsOn(":${module.module.id.name}:compileJava")
        reloadClassesTask.dependsOn("compileJava")
        return reloadClassesTask
    }

    /**
     * Creates a task that processes the resources of a given module and then copies them into
     * the build folder of the main module for Spring Boot Dev Tools to pick up for reload.
     */
    private fun createReloadResourcesTaskForModule(project: Project, module: ResolvedDependency): Task {
        val reloadResourcesTask = project.tasks.create("reloadResourcesFrom${module.moduleName}", Copy::class.java)
        module.moduleArtifacts.forEach {
            if (it.file.path.endsWith(".jar")) {
                // A lot of assumptions here:
                // - we assume a single JAR dependency per module
                // - we assume the JAR file is in the build/libs folder
                // - we assume the resources of the module are available in build/resources
                // - we assume that Spring Boot dev tools is watching the build/resources folder of this project
                val classesFolder = File(it.file.parentFile.parentFile, "resources").absolutePath
                reloadResourcesTask.from(classesFolder)
                reloadResourcesTask.into("build/resources")
            }
        }

        // we assume that the module and the main module have the "java" plugin applied
        reloadResourcesTask.dependsOn(":${module.module.id.name}:processResources")
        reloadResourcesTask.dependsOn("processResources")
        return reloadResourcesTask
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

open class DevToolsPluginExtension(
        var projects: List<String>
)