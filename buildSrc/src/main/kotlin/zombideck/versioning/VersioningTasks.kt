package zombideck.versioning

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.util.Properties

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

