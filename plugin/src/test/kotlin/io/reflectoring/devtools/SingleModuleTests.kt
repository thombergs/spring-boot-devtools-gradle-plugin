package io.reflectoring.devtools

import io.reflectoring.devtools.BuildTaskAssert.Companion.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.io.File

class SingleModuleTests {

    companion object {
        const val WORK_DIR = "../samples/single-module"
    }

    @AfterEach
    fun cleanup() {
        val buildFile = File(WORK_DIR, "build.gradle")
        buildFile.delete()
    }

    @Test
    fun defaultConfiguration() {
        val workDir = prepareBuildfile("default-configuration.gradle")

        val files = Files()
        files.addJavaSourceFile("$WORK_DIR/src/main/java/io/reflectoring/devtools/Test.java", "io.reflectoring.devtools")
        files.expectTargetFile("$WORK_DIR/build/classes/java/main/io/reflectoring/devtools/Test.class")
        files.addPropertiesSourceFile("$WORK_DIR/src/main/resources/test.properties")
        files.expectTargetFile("$WORK_DIR/build/resources/main/test.properties")
        files.expectTargetFile("$WORK_DIR/build/.triggerFile")

        val resultAfterChange = GradleRunner.create()
                .withProjectDir(workDir)
                .withArguments("clean", "restart")
                .withPluginClasspath()
                .build()

        assertThat(resultAfterChange.task(":restart")).hasAnyOutcome(TaskOutcome.SUCCESS)
        assertThat(resultAfterChange.task(":compileJava")).hasAnyOutcome(TaskOutcome.SUCCESS)
        assertThat(resultAfterChange.task(":processResources")).hasAnyOutcome(TaskOutcome.SUCCESS)
        files.assertTargetFilesExist()
    }

    @Test
    fun customTriggerFile() {
        val workDir = prepareBuildfile("custom-trigger-file.gradle")

        val files = Files()
        files.expectTargetFile("$WORK_DIR/build/.customTriggerFile")

        val result = GradleRunner.create()
                .withProjectDir(workDir)
                .withArguments("clean", "restart")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":restart")).hasAnyOutcome(TaskOutcome.SUCCESS)
        assertThat(result.task(":compileJava")).hasAnyOutcome(TaskOutcome.SUCCESS)
        assertThat(result.task(":processResources")).hasAnyOutcome(TaskOutcome.SUCCESS)
        files.assertTargetFilesExist()
    }

    private fun prepareBuildfile(buildFileName: String): File {
        val workDir = File(WORK_DIR)
        val buildFile = File(workDir, buildFileName)
        val targetBuildFile = File(workDir, "build.gradle")
        buildFile.copyTo(targetBuildFile, overwrite = true)
        return workDir
    }


}