# Spring Boot Dev Tools Gradle Plugin

![CI](https://github.com/thombergs/spring-devtools-gradle-plugin/workflows/CI/badge.svg)

This plugin enables [Spring Boot Dev Tools](https://docs.spring.io/spring-boot/docs/current/reference/html/using-spring-boot.html#using-boot-devtools) in your Gradle-based project to improve the dev loop when working on your Spring Boot application.

**You simply call `./gradlew restart` on your Spring Boot project and changed files will be visible in your app within a few seconds.** Since it relies on Gradle, it works with any IDE (or, more precisely, without an IDE).

The plugin will collect all changed files **from all the Gradle modules your Spring Boot app depends on**. Spring Boot Dev Tools will then restart your Spring Boot application context to make the changes visible in your app without having to do a cold restart.

## How does it work?

The [Spring Boot Dev Tools](https://docs.spring.io/spring-boot/docs/current/reference/html/using-spring-boot.html#using-boot-devtools) can restart a Spring Boot application automatically when the classpath of the application changes. 

This plugin takes advantage of that and copies all changed files into the `build` folder of the Spring Boot app, which feeds into the classpath if you start the app with `./gradlew bootrun`. Spring Boot Dev Tools will then take over and restart the app.

### Samples

Look at the sample projects to see it in action:

* [single-module](samples/single-module/)
* [multi-module](samples/multi-module)

### Apply the plugin

Apply the plugin to the Gradle module containing the Spring Boot application:

```groovy
plugins {
  id 'org.springframework.boot' version '2.3.2.RELEASE'
  id 'io.spring.dependency-management' version '1.0.9.RELEASE'
  id 'java'
  id 'io.reflectoring.spring-boot-devtools' version '0.0.1' // <---
}
```

You don't need to specify the dependency to the Spring Boot Dev Tools, the plugin will take care of that.

### Configure a trigger file

Add this to your `application.yml`:

```yaml
devtools:
  restart:
    trigger-file: .triggerFile
    additional-paths: build
```

This configures Spring Boot Dev Tools to only restart the Spring Boot app when the file `/build/.triggerFile` changes. This file is touched by the plugin each time it has updated all the changed files.

### Start your Spring Boot app 
Start your Spring Boot app with `./gradlew bootrun`.

### Restart after changing files 
Run `./gradlew restart` any time you have changed a Java file or a resources file. The Spring Boot app should restart within a couple of seconds and if you have a [Live Reload plugin](http://livereload.com/) installed in your browser, the page should refresh and make the changes visible.

Configure this task to a hot key in your IDE to trigger the restart comfortably from within the IDE.

### Configure multi-module builds

What if you have a Gradle module for your Spring Boot app and one or more Gradle modules that contribute to its classpath? This was the main reason for developing this plugin!

The setup steps are the same as for a single module (see above), plus you have to specify which of the contributing modules you want to trigger a restart.

In the `build.gradle` of the Gradle module with the Spring Boot app, add the modules to the `restart` configuration in your `dependencies` section:

```groovy
dependencies {
 
  // these modules contribute a JAR file to the Spring Boot app 
  implementation project(':module1') 
  implementation project(':module2')

  // this enables auto-restart for changes in one of the modules
  restart project(':module1') // <---
  restart project(':module2') // <---

  // Spring Boot dependencies and other dependencies omitted
}
```

Every time you call `./gradlew restart` on the main Gradle module now, the plugin will trigger compilation in `module1` and `module2` and update the classpath with these files for Spring Boot Dev Tools to pick up.

The more modules in the `restart` configuration, the longer each restart will take, since each module has to be built before a restart. So choose only those modules which you are currently working on.

Have a look at the [multi-module sample](/samples/multi-module/) to see the plugin in action for Spring Boot apps that consist of multiple modules.

### Configuration options

The above works without any configuration, but if the defaults don't suit you, use the `devtools` closure in your `build.gradle` file to override them:

```groovy
devtools {
  
  // Change the name of the trigger file. The plugin creates this file in the "build" folder of the
  // main module after each call of the "restart" task. Make sure to configure the same trigger file
  // in devtools.restart.trigger-file in your application.yml or application.properties file.
  triggerFile = ".triggerFile"

  modules {
    // "module1" is a random, but unique, identifier for
    // the module we're configuring.
    module1 {                                  
      
      // Must be one of the modules in the restart configuration.
      dependency = ":module1"                   
      
      // This task will be called on the module to update the resource files in the class path.
      // It's expected to put all changed resources into the folder "build/resources/main".
      resourcesTask = "processResources"

      // this task will be called on the module to update the compiled Java classes in the class path.
      // It's expected to put all the changed class files into the folder "build/classes/java/main".
      classesTask = "compileJava"        
    }
  }
}
```

### Integrate with Node projects

One use case for custom tasks in `resourcesTask` and `classesTask` is a Gradle module that contains a Javascript project. The Javascript project uses Node to build a set of static Javascript and CSS files that we want to use in our Spring Boot application.

The Gradle module packages these resources in a JAR file which contributes to the class path of the Spring Boot application. 

For a restart, we don't want to package the JAR file, but instead only update the resources in the `build/resources` folder for the Spring Boot Dev Tools Plugin to pick up.

We can now create a custom Gradle task that call Node (for example with the [Gradle Node Plugin](https://github.com/node-gradle/gradle-node-plugin)) to process the changed resources and copy the result into the `build` folder. If we configure that task as the `resourcesTask` as shown above, the Spring Boot Dev Tools Plugin will then trigger a restart. 

## Limitations

The plugin has been tested with single- and multi-module Gradle builds in a pretty standard configuration that should cover the most common cases. The plugin currently works with these assumptions:

* the contributing modules have the `java` plugin installed
* the contributing modules produce a single JAR file in `build/libs`
* the build folder of the modules is `build`
* ... and probably a bunch of other assumptions that I haven't identified, yet.

Feel free to open issues with feature requests or pull requests with improvements!