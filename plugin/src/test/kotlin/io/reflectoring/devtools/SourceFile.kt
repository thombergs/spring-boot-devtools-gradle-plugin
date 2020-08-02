package io.reflectoring.devtools

import java.io.File
import java.io.FileWriter

/**
 * A file that is expected to be picked up in a source location by the plugin.
 */
data class SourceFile(
        val filepath: String,
        val content: String
) {
    fun create() {
        val file = File(filepath)
        file.parentFile.mkdirs()
        file.createNewFile()
        val writer = FileWriter(file)
        writer.write(content)
        writer.flush()
        writer.close()
    }

    fun delete(){
        File(this.filepath).delete()
    }
}

