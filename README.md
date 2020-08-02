# Spring Boot Dev Tools Gradle Plugin

This plugin enables Spring Boot Dev Tools in your Gradle-based project to improve the dev loop when working on your Spring Boot application.

You simply call `./gradlew restart` on your Spring Boot project and the plugin will collect all changed files **from all the Gradle modules your Spring Boot app depends on**. Spring Boot Dev Tools will then restart your Spring Boot application context to make the changes visible in your app without having to do a cold restart.

## How does it work?

### Single-module builds

Apply the plugin to the Gradle module containing the Spring Boot application:

```groovy
plugins {
	id 'org.springframework.boot' version '2.3.2.RELEASE'
	id 'io.spring.dependency-management' version '1.0.9.RELEASE'
	id 'java'
	id 'io.reflectoring.spring-boot-devtools' // <---
}
```

Add this to your `application.yml`:

```yaml
devtools:
  restart:
  trigger-file: .triggerFile
  additional-paths: build
```

This configures Spring Boot Dev Tools to only restart the Spring Boot app when the file `/build/.triggerFile` changes. This file is touched by the plugin each time it has updated all the changed files.

Start your Spring Boot app with `./gradlew bootrun` and run `./gradlew restart` any time you have changed a Java file or a resources file. The Spring Boot app should restart within a couple of seconds and if you have a Live Reload plugin installed in your browser, the page should refresh and make the changes visible.

Have a look at the [single-module sample](/samples/single-module/) to see the plugin in action for Spring Boot apps that consist of a single Gradle module only.

### Multi-module builds

The support for single-module builds is pretty good with only using Spring Boot Dev Tools so you wouldn't really need this plugin. **Where this plugin really shines is in its support for multi-module Gradle builds**.

The setup steps are the same as for a single module (see above), plus you have to specify which of the contributing modules you want to trigger a restart.

In your `build.gradle`, add these modules to the `restart` configuration in your `dependencies` section:

```groovy
dependencies {
  implementation project(':module1')
  implementation project(':module2')

  restart project(':module1') // <---
  restart project(':module2') // <---

  // Spring Boot dependencies and other dependencies omitted
}
```

Every time you call `./gradlew restart` on the main Gradle module now, the plugin will trigger compilation in `module1` and `module2` and update the classpath with these files for Spring Boot Dev Tools to pick up.

The more modules in the `restart` configuration, the longer each restart will take, since each module has to be built before a restart. So choose only those modules which you are currently working on.

Have a look at the [multi-module sample](/samples/multi-module/) to see the plugin in action for Spring Boot apps that consist of multiple modules.

### Custom tasks

The above works nicely for any standard Gradle module that has a `compileJava` and a `processResources` task (i.e. any module that applies the `java` plugin).

Both tasks will be called before a restart to update the class path of the Spring Boot application with updated files.

If you have a non-standard module that does not have these tasks, you can tell the Spring Boot Dev Tools plugin to call other tasks instead. You can configure this in the `devtools` closure in `build.gradle`:

```groovy
devtools {
  modules {
    module1 {                                  // <-- random, unique identifier
      dependency = ":module1"                  // <-- must be one of the modules in the restart configuration 
      resourcesTask = "customProcessResources" // <-- this task will be called to update the resources in the class path
      classesTask = "customCompileJava"        // <-- this task will be called to update the compiled Java classes in the class path
    }
  }
}
```

### Integration with Node projects

One use case for custom tasks is a Gradle module that contains a Javascript project. The Javascript project uses Node to build a set of static Javascript and CSS files that we want to use in our Spring Boot application.

The Gradle module packages these resources in a JAR file which contributes to the class path of the Spring Boot application. 

For a restart, we don't want to package the JAR file, but instead only update the resources in the `build/resources` folder for the Spring Boot Dev Tools Plugin to pick up.

We can now create a custom Gradle task that call Node (for example with the [Gradle Node Plugin](https://github.com/node-gradle/gradle-node-plugin)) to process the changed resources and copy the result into the `build` folder. If we configure that task as the `resourcesTask` as shown above, the Spring Boot Dev Tools Plugin will then trigger a restart. 