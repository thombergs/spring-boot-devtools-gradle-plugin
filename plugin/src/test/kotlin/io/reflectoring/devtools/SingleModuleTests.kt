package io.reflectoring.devtools

import io.reflectoring.devtools.BuildTaskAssert.Companion.assertThat
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileWriter

const val WORK_DIR = "../samples/single-module"
const val JAVA_SOURCE_FILE = "$WORK_DIR/src/main/java/io/reflectoring/devtools/Test.java"
const val JAVA_TARGET_FILE = "$WORK_DIR/build/classes/java/main/io/reflectoring/devtools/Test.class"
const val RESOURCES_SOURCE_FILE = "$WORK_DIR/src/main/resources/test.properties"
const val RESOURCES_TARGET_FILE = "$WORK_DIR/build/resources/main/test.properties"
const val TRIGGER_FILE = "$WORK_DIR/build/.triggerFile"

class SingleModuleTests {

    @AfterEach
    fun cleanup() {
        val buildFile = File(WORK_DIR, "build.gradle")
        buildFile.delete()
        File(JAVA_SOURCE_FILE).delete()
        File(RESOURCES_SOURCE_FILE).delete()
    }

    @Test
    fun defaultConfiguration() {
        val workDir = prepareBuildfile("default-configuration.gradle")

        val result = GradleRunner.create()
                .withProjectDir(workDir)
                .withArguments("clean", "restart")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":restart")).hasOutcome(TaskOutcome.SUCCESS)
        assertThat(result.task(":compileJava")).hasOutcome(TaskOutcome.SUCCESS)
        assertThat(result.task(":processResources")).hasOutcome(TaskOutcome.SUCCESS)
        assertThat(File(TRIGGER_FILE)).exists()

        addJavaFile()
        addResourcesFile()
        File(TRIGGER_FILE).delete()

        val resultAfterChange = GradleRunner.create()
                .withProjectDir(workDir)
                .withArguments("restart")
                .withPluginClasspath()
                .build()

        assertThat(resultAfterChange.task(":restart")).hasOutcome(TaskOutcome.SUCCESS)
        assertThat(resultAfterChange.task(":compileJava")).hasOutcome(TaskOutcome.SUCCESS)
        assertThat(resultAfterChange.task(":processResources")).hasOutcome(TaskOutcome.SUCCESS)
        assertThat(File(JAVA_TARGET_FILE).exists()).isTrue()
        assertThat(File(RESOURCES_TARGET_FILE).exists()).isTrue()
        assertThat(File(TRIGGER_FILE)).exists()
    }

    @Test
    fun customTriggerFile() {
        val workDir = prepareBuildfile("custom-trigger-file.gradle")

        val result = GradleRunner.create()
                .withProjectDir(workDir)
                .withArguments("clean", "restart")
                .withPluginClasspath()
                .build()

        assertThat(result.task(":restart")).hasOutcome(TaskOutcome.SUCCESS)
        assertThat(result.task(":compileJava")).hasOutcome(TaskOutcome.SUCCESS)
        assertThat(result.task(":processResources")).hasOutcome(TaskOutcome.SUCCESS)
        assertThat(File(WORK_DIR, "build/.customTriggerFile")).exists()
    }

    private fun addResourcesFile(): File {
        val file = File(RESOURCES_SOURCE_FILE)
        file.parentFile.mkdirs()
        file.createNewFile()
        val writer = FileWriter(file)
        writer.write("""
            foo=bar
        """.trimIndent())
        writer.flush()
        writer.close()
        return file
    }

    private fun addJavaFile(): File {
        val file = File(JAVA_SOURCE_FILE)
        file.parentFile.mkdirs()
        file.createNewFile()
        val writer = FileWriter(file)
        writer.write("""
            package io.reflectoring.devtools;

            public class Test {

                public static void main(String[] args) {
                    System.out.println("Hello world!");
                }
            }
        """.trimIndent())
        writer.flush()
        writer.close()
        return file
    }

    private fun prepareBuildfile(buildFileName: String): File {
        val workDir = File(WORK_DIR)
        val buildFile = File(workDir, buildFileName)
        val targetBuildFile = File(workDir, "build.gradle")
        buildFile.copyTo(targetBuildFile, overwrite = true)
        return workDir
    }


}