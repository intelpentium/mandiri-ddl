import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.isFile) file.inputStream().use { load(it) }
}
val tmdbToken = (providers.environmentVariable("TMDB_READ_ACCESS_TOKEN").orNull
    ?: localProperties.getProperty("TMDB_READ_ACCESS_TOKEN", "")).trim()
val tokenLiteral = tmdbToken.replace("\\", "\\\\").replace("\"", "\\\"")
    .replace("\n", "\\n").replace("\r", "\\r")

android {
    namespace = "com.example.testmandiri.data"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 24
        buildConfigField("String", "TMDB_READ_ACCESS_TOKEN", "\"$tokenLiteral\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = true
    }
}

ksp {
    arg("room.schemaLocation", file("schemas").path)
}

dependencies {
    api(libs.paging.common)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.gson)
    implementation(libs.room.runtime)
    implementation(libs.hilt.android)
    ksp(libs.room.compiler)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
}
