# Single-module sample

This sample shows how to configure the Spring Boot Dev Tools plugin for a single-module Gradle build.

Rename one of the following files to `build.gradle`:
* `default-configuration.gradle`
* `custom-configuration.gradle`

Then, start the Spring Boot app with `./gradlew bootrun`.

Change any of the files in the following locations while the app is running (or add new files):
* `/app/src/main/java`
* `/app/src/main/resources`

Then, run `./gradlew restart` and refresh the page. Your changes should be visible. 