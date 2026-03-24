plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val directorioAnalisis = layout.projectDirectory.dir("src/main/java/com/abn/pkm_forms/analisis")
val directorioGenerado = layout.buildDirectory.dir("generated-src/cupjflex/com/abn/pkm_forms/analisis")
val directorioGeneradoRaiz = layout.buildDirectory.dir("generated-src/cupjflex")

val configuracionJflex by configurations.creating
val configuracionCup by configurations.creating

val generarLexer by tasks.registering(JavaExec::class) {
    group = "generacion"
    description = "Genera scanner con JFlex"
    classpath = configuracionJflex
    mainClass.set("jflex.Main")
    args(
        "-d",
        directorioGenerado.get().asFile.absolutePath,
        directorioAnalisis.file("AnalizadorJflex.flex").asFile.absolutePath
    )
    outputs.file(directorioGenerado.get().file("AnalizadorJflex.java"))
}

val generarParser by tasks.registering(JavaExec::class) {
    group = "generacion"
    description = "Genera parser con CUP"
    classpath = configuracionCup
    mainClass.set("java_cup.Main")
    args(
        "-destdir",
        directorioGenerado.get().asFile.absolutePath,
        "-parser",
        "parser",
        directorioAnalisis.file("Parser.cup").asFile.absolutePath
    )
    outputs.file(directorioGenerado.get().file("parser.java"))
    outputs.file(directorioGenerado.get().file("sym.java"))
}

tasks.matching { it.name == "preBuild" }.configureEach {
    dependsOn(generarLexer, generarParser)
}

android {
    namespace = "com.abn.pkm_forms"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.abn.pkm_forms"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }

    sourceSets {
        getByName("main") {
            java.srcDir(directorioGeneradoRaiz.get().asFile.absolutePath)
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.java.cup.runtime)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    add("configuracionJflex", libs.jflex)
    add("configuracionCup", libs.java.cup)
}
