# Spring Boot Dev Tools Gradle Plugin

![CI](https://github.com/thombergs/spring-devtools-gradle-plugin/workflows/CI/badge.svg)

This plugin enables [Spring Boot Dev Tools](https://docs.spring.io/spring-boot/docs/current/reference/html/using-spring-boot.html#using-boot-devtools) in your Gradle-based project to improve the dev loop when working on your Spring Boot application.

This plugin brings the following tasks:

* `./gradlew restart` to trigger a restart of the Spring application context when you have changed a Java file
* `./gradlew reload` to trigger a refresh of static resources when you have changed HTML templates, images, or other static resources

Run these tasks while you have started a Spring Boot application via `./gradlew bootrun` and the changes you made will become visible in the application after a couple seconds. A reload is quicker than a restart because it doesn't need to restart the Spring application context.

## Configuration

### Apply the plugin

Apply the plugin to the Gradle module containing the Spring Boot application:

```groovy
plugins {
  id 'org.springframework.boot' version '2.3.2.RELEASE'
  id 'io.spring.dependency-management' version '1.0.9.RELEASE'
  id 'java'
  id 'io.reflectoring.spring-boot-devtools' version '0.0.2' // <---
}
```

### Configure a trigger file

Add this to your `application.yml`:

```yaml
devtools:
  restart:
    trigger-file: .triggerFile
    additional-paths: build
```

This configures Spring Boot Dev Tools to only restart the Spring Boot app when the file `/build/.triggerFile` changes. This file is touched by the plugin each time it has updated all the changed files.

### Configure contributing modules

If you want to see changes in contributing modules (i.e. Gradle modules that contribute a JAR file to the Spring Boot application), you have to declare if you want them to be included in a restart and a reload by adding them to the `restart` and/or `reload` configuration:

```groovy
dependencies {
 
  // these modules contribute a JAR file to the Spring Boot app 
  implementation project(':module1') 
  implementation project(':module2')

  // this enables auto-restart for changes in module1
  restart project(':module1') 
  
  // this enables auto-reload for changes in module2
  reload project(':module2')

  // Spring Boot dependencies and other dependencies omitted
}
```

Running `./gradlew restart` will now copy all compiled Java files into the `build/classes` folder of the Spring Boot module to refresh the classpath and trigger a restart.

Running `./gradlew reload` will now copy all files from `src/main/resources` from the module into the `build/resources` folder of the Spring Boot module to refresh the classpath and trigger a reload.

If a module contributes resource files from a different location than `src/main/resources`, you can specify a custom Gradle task to be called on `restart` for each module:

```groovy
devtools {

  modules {
    // "module1" is a random, but unique, identifier for
    // the module we're configuring.
    module1 {                                  
      
      // Must be one of the modules in the restart configuration.
      dependency = ":module1"                   
      
      // This task will be called every time `./gradlew reload` is called. 
      // It's expected to all changed resources into the folder "build/resources/main". From there, they
      // will automatically be copied to the main module. Alternatively, this task can copy any changed files 
      // directly into the "build/resources/main" folder of the main module.
      reloadTask = "reload"

    }
  }
}
```

### Configure a custom trigger file

If the default trigger file `.triggerFile` does not suit you, you can change to name othe trigger file:

```groovy
devtools {
  // Change the name of the trigger file. The plugin creates this file in the "build" folder of the
  // main module after each call of the "restart" task. Make sure to configure the same trigger file
  // in devtools.restart.trigger-file in your application.yml or application.properties file.
  triggerFile = ".triggerFile"
}
```

## Samples

Look at the sample projects to see the plugin in action:

* [single-module](samples/single-module)
* [multi-module](samples/multi-module)

## Limitations

The plugin has been tested with single- and multi-module Gradle builds in a pretty standard configuration that should cover the most common cases. The plugin has also been successfully tested with NPM projects (wrapped in Gradle) that provide Javascript resources to a Spring Boot application.  

The plugin currently works with these assumptions:

* the contributing modules have the `java` plugin installed
* the contributing modules produce a single JAR file in `build/libs`
* the build folder of the modules is `build`
* ... and probably a bunch of other assumptions that I haven't identified, yet.

Feel free to open issues with feature requests or pull requests with improvements!