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
     * The task that creates the resources files that should be updated by Spring Boot Dev Tools.
     * By default, this is the "processResources" task, which puts the resource files into the "build/resources" folder.
     */
    var resourcesTask: String? = "processResources"

    /**
     * The task that creates the class files that should be updated by Spring Boot Dev Tools.
     * By default, this is the "compileJava" task, which puts the class files into the "build/classes" folder.
     */
    var classesTask: String? = "compileJava"

    constructor(name: String, dependency: String) : this(name) {
        this.dependency = dependency
    }

}