package zombideck.versioning

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault
import java.io.ByteArrayOutputStream
import java.util.Properties
import javax.inject.Inject

data class AppVersion(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val code: Int,
) {
    val name: String
        get() = "$major.$minor.$patch"
}

internal fun parseVersion(versionName: String, versionCode: String, source: String): AppVersion {
    val parts = versionName.split('.')
    if (parts.size != 3) {
        throw GradleException("VERSION_NAME must follow semantic format X.Y.Z in $source.")
    }

    return AppVersion(
        major = parts[0].toIntOrNull() ?: throw GradleException("Invalid major version in VERSION_NAME: $versionName"),
        minor = parts[1].toIntOrNull() ?: throw GradleException("Invalid minor version in VERSION_NAME: $versionName"),
        patch = parts[2].toIntOrNull() ?: throw GradleException("Invalid patch version in VERSION_NAME: $versionName"),
        code = versionCode.toIntOrNull() ?: throw GradleException("Invalid VERSION_CODE: $versionCode"),
    )
}

internal fun bumpVersion(current: AppVersion, bumpType: String): AppVersion = when (bumpType) {
    "patch" -> current.copy(patch = current.patch + 1, code = current.code + 1)
    "minor" -> current.copy(minor = current.minor + 1, patch = 0, code = current.code + 1)
    "major" -> current.copy(major = current.major + 1, minor = 0, patch = 0, code = current.code + 1)
    else -> throw GradleException("Unknown bump type: $bumpType")
}

@DisableCachingByDefault(because = "Versioning tasks are quick and modify repository state.")
abstract class VersionFileTask : DefaultTask() {
    @get:InputFile
    abstract val versionFile: RegularFileProperty

    protected fun readVersion(): AppVersion {
        val file = versionFile.get().asFile
        if (!file.exists()) {
            throw GradleException("Missing ${file.name}. Create it before running version tasks.")
        }

        val properties = Properties().apply {
            file.inputStream().use(::load)
        }

        val versionName = properties.getProperty("VERSION_NAME")
            ?: throw GradleException("VERSION_NAME is missing in ${file.name}.")
        val versionCode = properties.getProperty("VERSION_CODE")
            ?: throw GradleException("VERSION_CODE is missing in ${file.name}.")

        return parseVersion(versionName, versionCode, file.name)
    }

    protected fun writeVersion(version: AppVersion) {
        val file = versionFile.get().asFile
        val properties = Properties().apply {
            setProperty("VERSION_NAME", version.name)
            setProperty("VERSION_CODE", version.code.toString())
        }
        file.writer().use { writer ->
            properties.store(writer, "Managed by Gradle version tasks")
        }
    }
}

@DisableCachingByDefault(because = "Print task does not produce cacheable outputs.")
abstract class PrintVersionTask : VersionFileTask() {
    @TaskAction
    fun printVersion() {
        val version = readVersion()
        logger.lifecycle("VERSION_NAME=${version.name}")
        logger.lifecycle("VERSION_CODE=${version.code}")
    }
}

@DisableCachingByDefault(because = "Version bump updates a tracked file.")
abstract class BumpVersionTask : VersionFileTask() {
    @get:Input
    abstract val bumpType: Property<String>

    @TaskAction
    fun bump() {
        val updated = bumpVersion(readVersion(), bumpType.get())
        writeVersion(updated)
        logger.lifecycle("Version updated to ${updated.name} (code ${updated.code})")
    }
}

@DisableCachingByDefault(because = "Release task mutates git state, pushes a branch, and opens a PR.")
abstract class ReleaseVersionTask : VersionFileTask() {
    @get:Input
    abstract val bumpType: Property<String>

    @get:Input
    abstract val remoteName: Property<String>

    @get:InputDirectory
    abstract val repoDir: DirectoryProperty

    @get:Inject
    abstract val execOperations: ExecOperations

    @TaskAction
    fun release() {
        ensureCleanGitState()
        ensureOnMainBranch()

        val updated = bumpVersion(readVersion(), bumpType.get())
        val tag = "v${updated.name}"
        val releaseBranch = "release/$tag"

        ensureTagDoesNotExist(tag)
        ensureBranchDoesNotExist(releaseBranch)
        writeVersion(updated)

        runGit("checkout", "-b", releaseBranch)
        runGit("add", versionFile.get().asFile.absolutePath)
        runGit("commit", "-m", "chore(release): $tag")
        runGit("push", "-u", remoteName.get(), releaseBranch)

        val prUrl = runGh(
            "pr", "create",
            "--base", "main",
            "--head", releaseBranch,
            "--title", "chore(release): $tag",
            "--body", "Automated release PR for $tag. Merging will tag `main` and publish the signed APK.",
        )
        runGh("pr", "merge", releaseBranch, "--auto", "--squash", "--delete-branch")
        runGit("checkout", "main")

        logger.lifecycle("Release PR opened and set to auto-merge: $prUrl")
    }

    private fun ensureCleanGitState() {
        val status = runGit("status", "--porcelain")
        if (status.isNotBlank()) {
            throw GradleException("Working tree is not clean. Commit or stash changes before running a release task.")
        }
    }

    private fun ensureOnMainBranch() {
        if (currentBranch() != "main") {
            throw GradleException("Release tasks must be run from an up-to-date main branch.")
        }
    }

    private fun ensureTagDoesNotExist(tag: String) {
        val localTag = runGitAllowFailure("rev-parse", "-q", "--verify", "refs/tags/$tag")
        if (localTag.first == 0) {
            throw GradleException("Tag $tag already exists locally.")
        }

        val remoteTag = runGitAllowFailure("ls-remote", "--tags", remoteName.get(), tag)
        if (remoteTag.first == 0 && remoteTag.second.isNotBlank()) {
            throw GradleException("Tag $tag already exists on remote ${remoteName.get()}.")
        }
    }

    private fun ensureBranchDoesNotExist(branch: String) {
        val remoteBranch = runGitAllowFailure("ls-remote", "--heads", remoteName.get(), branch)
        if (remoteBranch.first == 0 && remoteBranch.second.isNotBlank()) {
            throw GradleException("Branch $branch already exists on remote ${remoteName.get()}.")
        }
    }

    private fun currentBranch(): String {
        return runGit("rev-parse", "--abbrev-ref", "HEAD")
    }

    private fun runGit(vararg args: String): String {
        val result = runGitAllowFailure(*args)
        if (result.first != 0) {
            throw GradleException("git ${args.joinToString(" ")} failed: ${result.second}")
        }
        return result.second
    }

    private fun runGitAllowFailure(vararg args: String): Pair<Int, String> {
        val output = ByteArrayOutputStream()
        val result = execOperations.exec {
            workingDir = repoDir.get().asFile
            commandLine("git", *args)
            standardOutput = output
            errorOutput = output
            isIgnoreExitValue = true
        }
        return result.exitValue to output.toString().trim()
    }

    private fun runGh(vararg args: String): String {
        val output = ByteArrayOutputStream()
        val result = execOperations.exec {
            workingDir = repoDir.get().asFile
            commandLine("gh", *args)
            standardOutput = output
            errorOutput = output
            isIgnoreExitValue = true
        }
        if (result.exitValue != 0) {
            throw GradleException(
                "gh ${args.joinToString(" ")} failed: ${output.toString().trim()}\n" +
                    "Make sure the GitHub CLI (gh) is installed and authenticated (gh auth login).",
            )
        }
        return output.toString().trim()
    }
}


