import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class GenerateSourceTask() : DefaultTask() {
    @Input
    lateinit var outputDir: String

    @Input
    lateinit var packageTarget: String

    @TaskAction
    @Throws(Exception::class)
    abstract fun generate()
}