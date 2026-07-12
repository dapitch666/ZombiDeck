import com.android.build.api.dsl.ApplicationExtension
import java.util.Properties

val versionPropertiesFile: File = rootProject.file("version.properties")
val versionProperties = Properties().apply {
    if (!versionPropertiesFile.exists()) {
        error("Missing ${versionPropertiesFile.name}. Create it at project root.")
    }
    versionPropertiesFile.inputStream().use(::load)
}
val appVersionName = versionProperties.getProperty("VERSION_NAME")
    ?: error("VERSION_NAME is missing in ${versionPropertiesFile.name}")
val appVersionCode = versionProperties.getProperty("VERSION_CODE")?.toIntOrNull()
    ?: error("VERSION_CODE is missing or invalid in ${versionPropertiesFile.name}")

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

configure<ApplicationExtension> {
    namespace = "org.anne.zombideck"
    compileSdk = 37

    defaultConfig {
        applicationId = "org.anne.zombideck"
        minSdk = 26
        targetSdk = 37
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "org.anne.zombideck.HiltTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    val keystorePath = System.getenv("KEYSTORE_PATH")
    val keystorePassword = System.getenv("KEYSTORE_PASSWORD")
    val keyAlias = System.getenv("KEY_ALIAS")
    val keyPassword = System.getenv("KEY_PASSWORD")
    val hasReleaseSigningConfig =
        !keystorePath.isNullOrBlank() &&
            !keystorePassword.isNullOrBlank() &&
            !keyAlias.isNullOrBlank() &&
            !keyPassword.isNullOrBlank()

    if (hasReleaseSigningConfig) {
        signingConfigs {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = keystorePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (hasReleaseSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Configure Kotlin toolchain at module scope (Project/Kotlin extension receiver).
kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("com.google.dagger:hilt-android:2.60.1")
    implementation("androidx.compose.foundation:foundation-layout")
    ksp("com.google.dagger:hilt-android-compiler:2.60.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.4.0")
    implementation("androidx.preference:preference-ktx:1.2.1")

    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.1")

    implementation("androidx.core:core-ktx:1.19.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation(platform("androidx.compose:compose-bom:2026.06.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.9.8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.06.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.60.1")
    androidTestImplementation("androidx.multidex:multidex:2.0.1")
    androidTestImplementation("androidx.test:runner:1.7.0")
    kspTest("com.google.dagger:hilt-android-compiler:2.60.1")
    kspAndroidTest("com.google.dagger:hilt-android-compiler:2.60.1")

    androidTestImplementation("androidx.navigation:navigation-testing:2.9.8")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}