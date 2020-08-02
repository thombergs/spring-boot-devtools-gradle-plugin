package io.reflectoring.devtools

import java.io.File

/**
 * A file that is expected to be created in a target location by the plugin.
 */
data class TargetFile(
        val filepath: String
) {
    fun exists(): Boolean {
        return File(this.filepath).exists()
    }

    fun delete(){
        File(this.filepath).delete()
    }
}