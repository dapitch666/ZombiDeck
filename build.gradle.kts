
import zombideck.versioning.BumpVersionTask
import zombideck.versioning.PrintVersionTask
import zombideck.versioning.ReleaseVersionTask

// Top-level build file where you can add configuration options common to all subprojects/modules.
plugins {
    id("com.android.application") version "9.2.1" apply false
    id("com.google.dagger.hilt.android") version "2.60" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.0" apply false
    id("com.google.devtools.ksp") version "2.3.9" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.20" apply false
}

val appVersionFile: RegularFile = layout.projectDirectory.file("version.properties")

tasks.register<PrintVersionTask>("printVersion") {
    group = "versioning"
    description = "Print current VERSION_NAME and VERSION_CODE."
    versionFile.set(appVersionFile)
}

fun registerBumpTask(taskName: String, bumpType: String) {
    tasks.register<BumpVersionTask>(taskName) {
        group = "versioning"
        description = "Bump $bumpType version in version.properties."
        versionFile.set(appVersionFile)
        this.bumpType.set(bumpType)
    }
}

fun registerReleaseTask(taskName: String, bumpType: String) {
    tasks.register<ReleaseVersionTask>(taskName) {
        group = "release"
        description = "Bump $bumpType version, commit, tag, and push to remote."
        versionFile.set(appVersionFile)
        this.bumpType.set(bumpType)
        remoteName.set("origin")
        repoDir.set(layout.projectDirectory)
    }
}

registerBumpTask("bumpPatch", "patch")
registerBumpTask("bumpMinor", "minor")
registerBumpTask("bumpMajor", "major")

registerReleaseTask("releasePatch", "patch")
registerReleaseTask("releaseMinor", "minor")
registerReleaseTask("releaseMajor", "major")

