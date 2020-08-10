package io.reflectoring.devtools

import groovy.lang.Closure
import org.gradle.api.NamedDomainObjectContainer
import kotlin.streams.asSequence

/**
 * Entrypoint to the DSL that can be used to configure the DevToolsPlugin.
 */
open class DevToolsPluginConfig(
        var modules: NamedDomainObjectContainer<ModuleConfig>
) {

    /**
     * The name of the trigger file. The plugin will create this file in the build folder after all files have
     * been copied. Default: ".triggerFile".
     * <p>
     * Use the same file name to configure the property "devtools.restart.trigger-file" in your application.yml.
     */
    var triggerFile: String = ".triggerFile"

    fun modules(closure: Closure<ModuleConfig>) {
        modules.configure(closure)
    }

    /**
     * Returns the ModuleConfig for the given dependency. Returns a default ModuleConfig if no ModuleConfig exists
     * for the given dependency.
     * @param dependency a Gradle module dependency string (for example ":common:logging" when the module is in the folder "/common/logging").
     */
    internal fun getModuleConfig(dependency: String): ModuleConfig {
        val moduleConfig = modules.stream().asSequence()
                .filter { it.dependency == dependency }
                .firstOrNull()

        return moduleConfig ?: ModuleConfig("default", dependency)
    }
}

/**
 * Configures parameters for a module.
 */
open class ModuleConfig(
        /**
         * The name of the config. This is used as the key in the NamedDomainObjectContainer.
         */
        val name: String
) {

    /**
     * The dependency string to the module that is to be configured. This string should be the same as defined
     * in the "dependencies" closure in build.gradle (for example ":common:logging").
     */
    var dependency: String = "default"

    /**
     * An additional task that should be called for this module before triggering a reload in Spring Boot Dev Tools.
     * The task is expected to contribute static resources to the "build/classes" folder of the module.
     * By default, a task named "reload" will be created that copies all files from "src/main/resources/static" and
     * "src/main/resources/templates". The task defined here will be called in addition to the default task.
     */
    var reloadTask: String? = null

    constructor(name: String, dependency: String) : this(name) {
        this.dependency = dependency
    }

}