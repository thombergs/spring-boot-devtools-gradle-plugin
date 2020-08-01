package io.reflectoring.devtools

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.artifacts.UnknownConfigurationException
import org.gradle.api.tasks.Copy
import java.io.File

class DevToolsPlugin : Plugin<Project> {

    companion object {
        val EXTENSION_NAME = "reload"
    }

    lateinit var project: Project

    override fun apply(project: Project) {
        this.project = project
        createExtension()

        val reloadConfiguration = addOrCreateConfiguration(project, "reload")

        project.afterEvaluate {
            addDevToolsDependency(project)
            val tasks = mutableListOf<Task>()
            reloadConfiguration.resolve()
            reloadConfiguration.resolvedConfiguration.firstLevelModuleDependencies.forEach { dependency ->
                tasks.add(createReloadClassesTaskForModule(dependency))
                tasks.add(createReloadResourcesTaskForModule(dependency))
            }
            createAggregateReloadTask(project, tasks)
        }

    }

    private fun createExtension() {
        val modules = project.container(ModuleConfig::class.java)
        val config = DevToolsPluginConfig(modules)
        project.extensions.add(EXTENSION_NAME, config)
    }

    private fun getExtension(): DevToolsPluginConfig {
        return project.extensions.findByName(EXTENSION_NAME) as DevToolsPluginConfig
    }

    private fun createAggregateReloadTask(project: Project, tasks: List<Task>) {
        val reloadTask = project.tasks.create("reload")
        reloadTask.actions.add(Action {
            touchTriggerFile()
        })
        for (task in tasks) {
            reloadTask.dependsOn(task)
        }
        reloadTask.dependsOn("compileJava")
        reloadTask.dependsOn("processResources")
    }

    /**
     * Creates a task that compiles the Java files in a given module and then copies them into
     * the build folder of the main module for Spring Boot Dev Tools to pick up for reload.
     */
    private fun createReloadClassesTaskForModule(module: ResolvedDependency): Task {
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

                // we assume that the JAR file is in "build/libs"
                val dependencyRootDir = it.file.parentFile.parentFile.parentFile
                val dependencyString = toFullDependencyString(dependencyRootDir)
                // we assume that the module and the main module have the "java" plugin applied

                val moduleConfig = getExtension().getModuleConfig(dependencyString)

                reloadClassesTask.dependsOn("${moduleConfig.dependency}:${moduleConfig.classesTask}")
                return reloadClassesTask
            }
        }

        throw IllegalStateException("Module ${module.moduleName} does not publish a JAR file!")
    }

    /**
     * Creates a task that processes the resources of a given module and then copies them into
     * the build folder of the main module for Spring Boot Dev Tools to pick up for reload.
     */
    private fun createReloadResourcesTaskForModule(module: ResolvedDependency): Task {
        val reloadResourcesTask = project.tasks.create("reloadResourcesFrom${module.moduleName}", Copy::class.java)
        module.moduleArtifacts.forEach {
            if (it.file.path.endsWith(".jar")) {
                // A lot of assumptions here:
                // - we assume a single JAR dependency per module
                // - we assume the JAR file is in the build/libs folder
                // - we assume the resources of the module are available in build/resources
                // - we assume that Spring Boot dev tools is watching the build/resources folder of this project
                val resourcesFolder = File(it.file.parentFile.parentFile, "resources").absolutePath
                reloadResourcesTask.from(resourcesFolder)
                reloadResourcesTask.into("build/resources")

                // we assume that the JAR file is in "build/libs"
                val dependencyRootDir = it.file.parentFile.parentFile.parentFile
                val dependencyString = toFullDependencyString(dependencyRootDir)
                // we assume that the module and the main module have the "java" plugin applied

                val moduleConfig = getExtension().getModuleConfig(dependencyString)

                reloadResourcesTask.dependsOn("${moduleConfig.dependency}:${moduleConfig.resourcesTask}")
                return reloadResourcesTask
            }
        }

        throw IllegalStateException("Module ${module.moduleName} does not publish a JAR file!")
    }

    private fun touchTriggerFile() {
        val triggerFile = File(project.buildDir, ".triggerFile")
        if (!triggerFile.exists()) {
            triggerFile.createNewFile()
        } else {
            triggerFile.setLastModified(System.currentTimeMillis())
        }
    }

    private fun toFullDependencyString(dependencyRootDir: File): String {
        val rootDir = project.rootProject.rootDir
        val relativePath = dependencyRootDir.absolutePath.replaceFirst(rootDir.absolutePath, "")
        return relativePath.replace("/", ":")
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