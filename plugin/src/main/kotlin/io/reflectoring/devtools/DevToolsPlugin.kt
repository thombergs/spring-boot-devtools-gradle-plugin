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
        const val EXTENSION_NAME = "devtools"
        const val RESTART_CONFIGURATION_NAME = "restart"
        const val RESTART_TASK_NAME = "restart"
        const val RELOAD_CONFIGURATION_NAME = "reload"
        const val RELOAD_TASK_NAME = "reload"

        // Default list of folders that Spring Boot Dev Tools will not trigger a restart for
        // when a file in them changes.
        val RESOURCES_EXCLUDED_FROM_RESTART = listOf(
                "/META-INF/maven/**",
                "/META-INF/resources/**",
                "/resources/**",
                "/static/**",
                "/public/**",
                "/templates/**"
        )
    }

    lateinit var project: Project

    override fun apply(project: Project) {
        this.project = project
        createExtension()

        val restartConfiguration = addOrCreateConfiguration(project, RESTART_CONFIGURATION_NAME)
        val reloadConfiguration = addOrCreateConfiguration(project, RELOAD_CONFIGURATION_NAME)

        project.afterEvaluate {
            addDevToolsDependency(project)
            val reloadTask = addReloadTask(reloadConfiguration, project)
            addRestartTask(restartConfiguration, project, reloadTask)
        }

    }

    private fun addRestartTask(restartConfiguration: Configuration, project: Project, reloadTask: Task) {
        val subTasks = mutableListOf(reloadTask)
        restartConfiguration.resolve()
        restartConfiguration.resolvedConfiguration.firstLevelModuleDependencies.forEach { dependency ->
            subTasks.add(createRestartModuleTask(dependency))
        }
        createRestartTask(project, subTasks)
    }

    private fun addReloadTask(reloadConfiguration: Configuration, project: Project) : Task{
        val subTasks = mutableListOf<Task>()
        reloadConfiguration.resolve()
        reloadConfiguration.resolvedConfiguration.firstLevelModuleDependencies.forEach { dependency ->
            subTasks.add(createReloadModuleTask(dependency))
        }
        return createReloadTask(project, subTasks)
    }


    /**
     * Creates the "reload" task for the main module that contains the Spring Boot application.
     * The reload task updates all static resources that don't need a restart and creates a trigger file
     * to trigger Spring Boot Dev Tools for a reload.
     */
    private fun createReloadTask(project: Project, tasks: List<Task>) : Task{
        val reloadTask = project.tasks.create(RELOAD_TASK_NAME, Copy::class.java)

        reloadTask.from("src/main/resources")
        reloadTask.into("build/resources/main")

        for(folder in RESOURCES_EXCLUDED_FROM_RESTART){
            reloadTask.include(folder)
        }

        println("CREATE RELOAD TASK")

        for (task in tasks) {
            println("ADDING TASK $task")
            reloadTask.dependsOn(task)
        }
        reloadTask.actions.add(Action {
            touchTriggerFile()
        })

        return reloadTask
    }

    /**
     * Creates the "restart" task for the main module that contains the Spring Boot application.
     * The restart task compiles the main modules sources, calls the restart tasks for all sub modules,
     * and creates a trigger file to trigger Spring Boot Dev Tools.
     */
    private fun createRestartTask(project: Project, tasks: List<Task>) {
        val restartTask = project.tasks.create(RESTART_TASK_NAME)
        restartTask.dependsOn("classes")
        for (task in tasks) {
            restartTask.dependsOn(task)
        }
        restartTask.actions.add(Action {
            touchTriggerFile()
        })
    }

    private fun createRestartModuleTask(module: ResolvedDependency): Task {
        val restartModuleTask = project.tasks.create("restart-${module.moduleName}", Copy::class.java)
        module.moduleArtifacts.forEach {
            if (it.type == "jar") {
                val dependencyRootDir = it.file.parentFile.parentFile.parentFile

                val classesSourceFolder = "${dependencyRootDir}/build/classes"
                val classesTargetFolder = "${project.buildDir}/classes"

                restartModuleTask.from(classesSourceFolder)
                restartModuleTask.into(classesTargetFolder)

                val dependencyString = toFullDependencyString(dependencyRootDir)
                val moduleConfig = getExtension().getModuleConfig(dependencyString)

                // we always want to run the "classes" task when restarting
                restartModuleTask.dependsOn("${moduleConfig.dependency}:classes")

                // if there is a custom reload task, we want to run that, too, when restarting
                moduleConfig.reloadTask?.let {
                    restartModuleTask.dependsOn("${moduleConfig.dependency}:${moduleConfig.reloadTask}")
                }
                return restartModuleTask
            }
        }

        throw IllegalStateException("Module ${module.moduleName} does not publish a JAR file!")
    }

    private fun createReloadModuleTask(module: ResolvedDependency): Task {
        val reloadModuleTask = project.tasks.create("reload-${module.moduleName}", Copy::class.java)
        module.moduleArtifacts.forEach {
            if (it.type == "jar") {
                val dependencyRootDir = it.file.parentFile.parentFile.parentFile

                val resourcesSourceFolder = "${dependencyRootDir}/src/main/resources"
                val resourcesTargetFolder = "${project.buildDir}/resources/main"

                reloadModuleTask.from(resourcesSourceFolder)
                reloadModuleTask.into(resourcesTargetFolder)

                for(folder in RESOURCES_EXCLUDED_FROM_RESTART){
                    reloadModuleTask.include(folder)
                }

                val dependencyString = toFullDependencyString(dependencyRootDir)
                val moduleConfig = getExtension().getModuleConfig(dependencyString)

                // if there is a custom reload task, we want to run that when reloading
                moduleConfig.reloadTask?.let {
                    reloadModuleTask.dependsOn("${moduleConfig.dependency}:${moduleConfig.reloadTask}")
                }
                return reloadModuleTask
            }
        }

        throw IllegalStateException("Module ${module.moduleName} does not publish a JAR file!")
    }

    private fun createExtension() {
        val modules = project.container(ModuleConfig::class.java)
        project.extensions.create(EXTENSION_NAME, DevToolsPluginConfig::class.java, modules)
    }

    private fun getExtension(): DevToolsPluginConfig {
        return project.extensions.findByName(EXTENSION_NAME) as DevToolsPluginConfig
    }

    private fun touchTriggerFile() {
        val triggerFileName = getExtension().triggerFile
        val triggerFile = File(project.buildDir, triggerFileName)
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