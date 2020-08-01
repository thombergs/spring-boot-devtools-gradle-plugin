package io.reflectoring.devtools

import org.assertj.core.api.AbstractAssert
import org.gradle.testkit.runner.BuildTask
import org.gradle.testkit.runner.TaskOutcome
import java.util.*

class BuildTaskAssert(actual: BuildTask?) : AbstractAssert<BuildTaskAssert, BuildTask>(actual, BuildTaskAssert::class.java) {

    companion object {
        fun assertThat(actual: BuildTask?): BuildTaskAssert {
            return BuildTaskAssert(actual)
        }
    }

    fun hasOutcome(expectedOutcome: TaskOutcome): BuildTaskAssert {
        isNotNull
        val actualOutcome = actual.outcome
        if (!Objects.equals(expectedOutcome, actualOutcome)) {
            failWithMessage("Task has outcome $actualOutcome, but expected $expectedOutcome")
        }
        return this
    }

}