var gitShaRevOutput = runProcessWithOutput("git log -1 --format=%h --abbrev=9 -- ${project.projectDir.absolutePath}") ?: "local"

if (gitShaRevOutput != "local") {
    val gitDiffProcess =
        runProcess("git diff --quiet --ignore-space-at-eol --text ${project.projectDir.absolutePath}/src/main/java/**/*.java ${project.projectDir.absolutePath}/*.gradle.kts ${project.projectDir.absolutePath}/gradle.properties ${project.projectDir.absolutePath}/src/main/resources/**")
    if (gitDiffProcess == 1) {
        project.logger.lifecycle("${project.name}: Uncommited changes detected.")
        gitShaRevOutput = "local"
    } else if (gitDiffProcess != 0) {
        project.logger.warn("${project.name}: Error whilst detecting changes. Is Git installed/working properly? Exit code: $gitDiffProcess")
        gitShaRevOutput = "local"
    }
}

project.extra["git_commit_sha"] = gitShaRevOutput.trim()

project.logger.lifecycle("${project.name}: Git commit SHA: ${project.extra["git_commit_sha"]}")
