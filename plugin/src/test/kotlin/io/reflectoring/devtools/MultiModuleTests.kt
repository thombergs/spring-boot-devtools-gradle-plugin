package io.reflectoring.devtools

import io.reflectoring.devtools.BuildTaskAssert.Companion.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.io.File

class MultiModuleTests {

    companion object {
        const val WORK_DIR = "../samples/multi-module"
    }

    @AfterEach
    fun cleanup() {
        val buildFile = File("$WORK_DIR/app", "build.gradle")
        buildFile.delete()
    }

    @Test
    fun restartWithDefaultConfiguration() {
        val workDir = prepareBuildfile("default-configuration.gradle")

        val files = restartFilesConfiguration()
        files.expectTargetFile("$WORK_DIR/app/build/.triggerFile")

        val result = GradleRunner.create()
                .withProjectDir(workDir)
                .withArguments("clean", "restart")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":app:restart")).hasAnyOutcome(TaskOutcome.SUCCESS)
        files.assertTargetFilesExist()
    }

    @Test
    fun restartWithCustomConfiguration() {
        val workDir = prepareBuildfile("custom-configuration.gradle")

        val files = restartFilesConfiguration()
        files.expectTargetFile("$WORK_DIR/app/build/.triggerFile")

        val result = GradleRunner.create()
                .withProjectDir(workDir)
                .withArguments("clean", "restart")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":app:restart")).hasAnyOutcome(TaskOutcome.SUCCESS)
        assertThat(result.task(":module1:customProcessResources")).hasAnyOutcome(TaskOutcome.SUCCESS, TaskOutcome.UP_TO_DATE)
        files.assertTargetFilesExist()
    }

    @Test
    fun reloadWithDefaultConfiguration() {
        val workDir = prepareBuildfile("default-configuration.gradle")

        val files = reloadFilesConfiguration()
        files.expectTargetFile("$WORK_DIR/app/build/.triggerFile")

        val result = GradleRunner.create()
                .withProjectDir(workDir)
                .withArguments("clean", "reload")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":app:reload")).hasAnyOutcome(TaskOutcome.SUCCESS)
        files.assertTargetFilesExist()
    }

    @Test
    fun reloadWithCustomConfiguration() {
        val workDir = prepareBuildfile("custom-configuration.gradle")

        val files = reloadFilesConfiguration()
        files.expectTargetFile("$WORK_DIR/app/build/.triggerFile")

        val result = GradleRunner.create()
                .withProjectDir(workDir)
                .withArguments("clean", "reload")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":app:reload")).hasAnyOutcome(TaskOutcome.SUCCESS)
        assertThat(result.task(":module1:customProcessResources")).hasAnyOutcome(TaskOutcome.SUCCESS, TaskOutcome.UP_TO_DATE)
        files.assertTargetFilesExist()
    }

    private fun restartFilesConfiguration() : Files{
        val files = reloadFilesConfiguration()
        files.addJavaSourceFile("$WORK_DIR/app/src/main/java/io/reflectoring/app/Test.java", "io.reflectoring.app")
        files.expectTargetFile("$WORK_DIR/app/build/classes/java/main/io/reflectoring/app/Test.class")

        files.addJavaSourceFile("$WORK_DIR/module1/src/main/java/io/reflectoring/module1/Test.java", "io.reflectoring.module1")
        files.expectTargetFile("$WORK_DIR/app/build/classes/java/main/io/reflectoring/module1/Test.class")

        files.addJavaSourceFile("$WORK_DIR/module2/src/main/java/io/reflectoring/module2/Test.java", "io.reflectoring.module2")
        files.expectTargetFile("$WORK_DIR/app/build/classes/java/main/io/reflectoring/module2/Test.class")

        return files
    }

    private fun reloadFilesConfiguration() : Files {
        val files = Files()
        files.addPropertiesSourceFile("$WORK_DIR/app/src/main/resources/templates/main.html")
        files.expectTargetFile("$WORK_DIR/app/build/resources/main/templates/main.html")

        files.addPropertiesSourceFile("$WORK_DIR/module1/src/main/resources/templates/module1.html")
        files.expectTargetFile("$WORK_DIR/app/build/resources/main/templates/module1.html")

        files.addPropertiesSourceFile("$WORK_DIR/module2/src/main/resources/templates/module2.html")
        files.expectTargetFile("$WORK_DIR/app/build/resources/main/templates/main.html")
        return files
    }

    private fun prepareBuildfile(buildFileName: String): File {
        val workDir = File("$WORK_DIR/app")
        val buildFile = File(workDir, buildFileName)
        val targetBuildFile = File(workDir, "build.gradle")
        buildFile.copyTo(targetBuildFile, overwrite = true)
        return workDir
    }


}