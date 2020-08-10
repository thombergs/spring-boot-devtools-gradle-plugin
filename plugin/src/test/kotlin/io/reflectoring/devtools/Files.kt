package io.reflectoring.devtools

import org.assertj.core.api.Assertions.assertThat


class Files(private val debug: Boolean = false) {

    private val sourceFiles: MutableList<SourceFile> = mutableListOf()
    private val targetFiles: MutableList<TargetFile> = mutableListOf()

    fun addSourceFile(filepath: String, content: String) {
        val file = SourceFile(filepath, content)
        file.create()
        this.sourceFiles.add(file)
    }

    fun addJavaSourceFile(filepath: String, packageName: String) {
        val file = SourceFile(filepath, """
            package $packageName;

            public class Test {

                public static void main(String[] args) {
                    System.out.println("Hello world!");
                }
            }
        """)
        file.create()
        this.sourceFiles.add(file)
    }

    fun addPropertiesSourceFile(filepath: String) {
        val file = SourceFile(filepath, """
            foo=bar
        """)
        file.create()
        this.sourceFiles.add(file)
    }

    fun expectTargetFile(filepath: String) {
        this.targetFiles.add(TargetFile(filepath))
    }

    fun assertTargetFilesExist() {
        for (file in targetFiles) {
            assertThat(file.exists())
                    .withFailMessage("Expected file ${file.filepath} to exist!")
                    .isTrue()


        }
        cleanup()
    }

    fun cleanup() {
        if (!debug) {
            for (file in sourceFiles) {
                file.delete()
            }
            for (file in targetFiles) {
                file.delete()
            }
        }
    }


}
