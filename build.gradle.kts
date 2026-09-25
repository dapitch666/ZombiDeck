import zombideck.versioning.BumpVersionTask
import zombideck.versioning.PrintVersionTask

// Top-level build file where you can add configuration options common to all subprojects/modules.
plugins {
    id("com.android.application") version "9.4.0" apply false
    id("com.google.dagger.hilt.android") version "2.60.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.20" apply false
    id("com.google.devtools.ksp") version "2.3.12" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.20" apply false
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

registerBumpTask("bumpPatch", "patch")
registerBumpTask("bumpMinor", "minor")
registerBumpTask("bumpMajor", "major")