import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginExtension
import java.io.BufferedReader
import java.io.InputStreamReader

fun Project.setBaseVersion(version: String) {
    project.version = version
    project.logger.lifecycle("${project.name}: project.version: ${project.version}")
}

fun Project.setBaseName(baseName: String) {
    extensions.configure<BasePluginExtension>("base") {
        archivesName.set(baseName)
    }
}

fun Project.runProcess(cmd: String): Int {
    val process = Runtime.getRuntime().exec(cmd)
    project.logger.lifecycle("${project.name}: Running command: $cmd")
    return process.waitFor()
}

fun Project.runProcessWithOutput(cmd: String): String? {
    val process = Runtime.getRuntime().exec(cmd)
    project.logger.lifecycle("${project.name}: Running command: $cmd")
    if (process.waitFor() != 0) {
        return null
    }
    BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
        val builder = StringBuilder()
        var r: String? = ""
        while (reader.readLine().also { r = it } != null) {
            builder.append(r)
        }
        return builder.toString()
    }
}
