# Wisdometer Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a complete offline Android app for tracking personal predictions with probability estimates and accuracy scoring.

**Architecture:** MVVM with Repository pattern — Room entities flow through DAOs into a repository, consumed by ViewModels via StateFlow, rendered with Jetpack Compose. Scoring is computed on-the-fly from resolved prediction data.

**Tech Stack:** Kotlin, Jetpack Compose (custom theme), Room, Hilt (KSP), WorkManager, Navigation Compose, kotlinx.serialization

---

## File Map

```
app/
  build.gradle.kts
  src/
    main/
      kotlin/com/wisdometer/
        WisdometerApp.kt                         # Application class, notification channel
        MainActivity.kt                          # Single activity, hosts NavHost
        di/
          AppModule.kt                           # Hilt module: repository binding
          DatabaseModule.kt                      # Hilt module: Room DB + DAOs
        data/
          db/
            WisdometerDatabase.kt                # Room DB definition
            Converters.kt                        # TypeConverters for Instant
          model/
            Prediction.kt                        # Room entity
            PredictionOption.kt                  # Room entity
            PredictionWithOptions.kt             # Room relation (Prediction + options)
          dao/
            PredictionDao.kt                     # DAO: CRUD + queries
          repository/
            PredictionRepository.kt              # Interface
            PredictionRepositoryImpl.kt          # Room-backed implementation
        domain/
          ScoringEngine.kt                       # Pure Kotlin: Brier + SimpleCloseness
        notifications/
          ReminderWorker.kt                      # WorkManager worker
          NotificationScheduler.kt               # Schedule/cancel reminders
        export/
          JsonExporter.kt                        # Serialize predictions to JSON
          JsonImporter.kt                        # Parse + merge JSON into DB
        share/
          ShareImageRenderer.kt                  # Render Compose to Bitmap, share
        ui/
          theme/
            Color.kt                             # App color palette
            Theme.kt                             # WisdometerTheme composable
            Type.kt                              # Typography
          navigation/
            NavGraph.kt                          # NavHost + destinations
          components/
            ProbabilityBar.kt                    # Horizontal bar for one option
            StatusBadge.kt                       # OPEN / RESOLVED chip
            PredictionCard.kt                    # Card shown in list
          predictions/
            PredictionsViewModel.kt
            PredictionsScreen.kt
          edit/
            EditPredictionViewModel.kt
            EditPredictionScreen.kt              # New + Edit (same composable)
          detail/
            PredictionDetailViewModel.kt
            PredictionDetailScreen.kt
          profile/
            ProfileViewModel.kt
            ProfileScreen.kt
            AccuracyChart.kt                     # Custom Canvas line chart
          settings/
            SettingsViewModel.kt
            SettingsScreen.kt
      res/
        values/
          strings.xml
          themes.xml                             # Empty theme (Compose controls all)
        xml/
          provider_paths.xml                     # FileProvider for image sharing
      AndroidManifest.xml
    test/
      kotlin/com/wisdometer/
        domain/
          ScoringEngineTest.kt
        export/
          JsonExporterTest.kt
          JsonImporterTest.kt
gradle/
  libs.versions.toml
settings.gradle.kts
build.gradle.kts                                 # Root build file
```

---

## Task 1: Project Scaffold

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle/libs.versions.toml`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/values/themes.xml`
- Create: `app/src/main/res/xml/provider_paths.xml`
- Create: `app/src/main/kotlin/com/wisdometer/WisdometerApp.kt`
- Create: `app/src/main/kotlin/com/wisdometer/MainActivity.kt`

- [ ] **Step 1: Create settings.gradle.kts**

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Wisdometer"
include(":app")
```

- [ ] **Step 2: Create gradle/libs.versions.toml**

```toml
[versions]
agp = "8.5.2"
kotlin = "2.0.21"
ksp = "2.0.21-1.0.27"
compose-bom = "2024.12.01"
room = "2.6.1"
hilt = "2.51.1"
hilt-navigation-compose = "1.2.0"
work-runtime = "2.10.0"
navigation-compose = "2.8.5"
kotlinx-serialization = "1.7.3"
lifecycle = "2.8.7"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version = "1.15.0" }
androidx-lifecycle-runtime = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version = "1.9.3" }
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-foundation = { group = "androidx.compose.foundation", name = "foundation" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-material-icons = { group = "androidx.compose.material", name = "material-icons-extended" }
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation-compose" }
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hilt-navigation-compose" }
work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "work-runtime" }
hilt-work = { group = "androidx.hilt", name = "hilt-work", version = "1.2.0" }
hilt-work-compiler = { group = "androidx.hilt", name = "hilt-compiler", version = "1.2.0" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
junit = { group = "junit", name = "junit", version = "4.13.2" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version = "1.9.0" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

- [ ] **Step 3: Create root build.gradle.kts**

```kotlin
// build.gradle.kts (root)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
```

- [ ] **Step 4: Create app/build.gradle.kts**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.wisdometer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.wisdometer"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.navigation.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)
    implementation(libs.kotlinx.serialization.json)
    debugImplementation(libs.compose.ui.tooling)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
```

- [ ] **Step 5: Create AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="28" />

    <application
        android:name=".WisdometerApp"
        android:allowBackup="true"
        android:label="@string/app_name"
        android:theme="@style/Theme.Wisdometer"
        android:supportsRtl="true">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/provider_paths" />
        </provider>

        <receiver android:name="androidx.work.impl.diagnostics.DiagnosticsReceiver"
            android:exported="true"
            android:permission="android.permission.DUMP" />
    </application>
</manifest>
```

- [ ] **Step 6: Create res files**

`app/src/main/res/values/strings.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Wisdometer</string>
    <string name="notification_channel_name">Prediction Reminders</string>
    <string name="notification_channel_desc">Reminders to resolve your predictions</string>
</resources>
```

`app/src/main/res/values/themes.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.Wisdometer" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
```

`app/src/main/res/xml/provider_paths.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <cache-path name="shared_images" path="images/" />
</paths>
```

- [ ] **Step 7: Create WisdometerApp.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/WisdometerApp.kt
package com.wisdometer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

const val REMINDER_CHANNEL_ID = "prediction_reminders"

@HiltAndroidApp
class WisdometerApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.notification_channel_name)
        val desc = getString(R.string.notification_channel_desc)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(REMINDER_CHANNEL_ID, name, importance).apply {
            description = desc
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
```

- [ ] **Step 8: Create MainActivity.kt (placeholder, full NavGraph wired in Task 7)**

```kotlin
// app/src/main/kotlin/com/wisdometer/MainActivity.kt
package com.wisdometer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.wisdometer.ui.navigation.NavGraph
import com.wisdometer.ui.theme.WisdometerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WisdometerTheme {
                NavGraph()
            }
        }
    }
}
```

- [ ] **Step 9: Verify the project builds**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL (may take several minutes on first run)

- [ ] **Step 10: Commit**

```bash
git init
git add .
git commit -m "chore: initial project scaffold"
```

---

## Task 2: Data Entities

**Files:**
- Create: `app/src/main/kotlin/com/wisdometer/data/model/Prediction.kt`
- Create: `app/src/main/kotlin/com/wisdometer/data/model/PredictionOption.kt`
- Create: `app/src/main/kotlin/com/wisdometer/data/model/PredictionWithOptions.kt`
- Create: `app/src/main/kotlin/com/wisdometer/data/db/Converters.kt`

- [ ] **Step 1: Create Prediction.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/data/model/Prediction.kt
package com.wisdometer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "predictions")
data class Prediction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val question: String,
    val createdAt: Instant = Instant.now(),
    val reminderAt: Instant? = null,
    val resolvedAt: Instant? = null,
    val outcomeOptionId: Long? = null,
    val tags: String = "",  // comma-separated, e.g. "career,finance"
)

val Prediction.tagList: List<String>
    get() = if (tags.isBlank()) emptyList() else tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
```

- [ ] **Step 2: Create PredictionOption.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/data/model/PredictionOption.kt
package com.wisdometer.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "prediction_options",
    foreignKeys = [
        ForeignKey(
            entity = Prediction::class,
            parentColumns = ["id"],
            childColumns = ["predictionId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("predictionId")],
)
data class PredictionOption(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val predictionId: Long,
    val label: String,
    val probability: Int,  // 0–100; all options for a prediction must sum to 100
    val sortOrder: Int,
)
```

- [ ] **Step 3: Create PredictionWithOptions.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/data/model/PredictionWithOptions.kt
package com.wisdometer.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class PredictionWithOptions(
    @Embedded val prediction: Prediction,
    @Relation(
        parentColumn = "id",
        entityColumn = "predictionId",
    )
    val options: List<PredictionOption>,
) {
    val isResolved: Boolean get() = prediction.resolvedAt != null
    val sortedOptions: List<PredictionOption> get() = options.sortedBy { it.sortOrder }
    val actualOption: PredictionOption? get() = options.find { it.id == prediction.outcomeOptionId }
}
```

- [ ] **Step 4: Create Converters.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/data/db/Converters.kt
package com.wisdometer.data.db

import androidx.room.TypeConverter
import java.time.Instant

class Converters {
    @TypeConverter
    fun fromInstant(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun toInstant(instant: Instant?): Long? = instant?.toEpochMilli()
}
```

- [ ] **Step 5: Commit**

```bash
git add app/src/main/kotlin/com/wisdometer/data/model/ app/src/main/kotlin/com/wisdometer/data/db/Converters.kt
git commit -m "feat: add Room data entities"
```

---

## Task 3: Room Database & DAO

**Files:**
- Create: `app/src/main/kotlin/com/wisdometer/data/dao/PredictionDao.kt`
- Create: `app/src/main/kotlin/com/wisdometer/data/db/WisdometerDatabase.kt`

- [ ] **Step 1: Create PredictionDao.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/data/dao/PredictionDao.kt
package com.wisdometer.data.dao

import androidx.room.*
import com.wisdometer.data.model.Prediction
import com.wisdometer.data.model.PredictionOption
import com.wisdometer.data.model.PredictionWithOptions
import kotlinx.coroutines.flow.Flow

@Dao
interface PredictionDao {

    @Transaction
    @Query("""
        SELECT * FROM predictions
        ORDER BY
          CASE
            WHEN reminderAt IS NOT NULL AND resolvedAt IS NULL AND reminderAt > :nowMs THEN 0
            ELSE 1
          END ASC,
          reminderAt ASC,
          CASE WHEN resolvedAt IS NULL THEN 0 ELSE 1 END ASC,
          createdAt DESC
    """)
    fun getAllWithOptions(nowMs: Long): Flow<List<PredictionWithOptions>>

    @Transaction
    @Query("SELECT * FROM predictions WHERE id = :id")
    fun getWithOptionsById(id: Long): Flow<PredictionWithOptions?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPrediction(prediction: Prediction): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertOptions(options: List<PredictionOption>)

    @Update
    suspend fun updatePrediction(prediction: Prediction)

    @Query("DELETE FROM prediction_options WHERE predictionId = :predictionId")
    suspend fun deleteOptionsForPrediction(predictionId: Long)

    @Delete
    suspend fun deletePrediction(prediction: Prediction)

    @Transaction
    suspend fun upsertPredictionWithOptions(prediction: Prediction, options: List<PredictionOption>) {
        val id = if (prediction.id == 0L) {
            insertPrediction(prediction)
        } else {
            updatePrediction(prediction)
            prediction.id
        }
        deleteOptionsForPrediction(id)
        insertOptions(options.map { it.copy(predictionId = id) })
    }

    // For import: check for duplicates by question + createdAt
    @Query("SELECT COUNT(*) FROM predictions WHERE question = :question AND createdAt = :createdAt")
    suspend fun countByQuestionAndCreatedAt(question: String, createdAt: Long): Int

    @Transaction
    @Query("SELECT * FROM predictions WHERE resolvedAt IS NOT NULL")
    suspend fun getAllResolvedWithOptions(): List<PredictionWithOptions>
}
```

- [ ] **Step 2: Create WisdometerDatabase.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/data/db/WisdometerDatabase.kt
package com.wisdometer.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wisdometer.data.dao.PredictionDao
import com.wisdometer.data.model.Prediction
import com.wisdometer.data.model.PredictionOption

@Database(
    entities = [Prediction::class, PredictionOption::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class WisdometerDatabase : RoomDatabase() {
    abstract fun predictionDao(): PredictionDao
}
```

- [ ] **Step 3: Verify compilation**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/kotlin/com/wisdometer/data/dao/ app/src/main/kotlin/com/wisdometer/data/db/WisdometerDatabase.kt
git commit -m "feat: add Room database and DAO"
```

---

## Task 4: Repository & Hilt Modules

**Files:**
- Create: `app/src/main/kotlin/com/wisdometer/data/repository/PredictionRepository.kt`
- Create: `app/src/main/kotlin/com/wisdometer/data/repository/PredictionRepositoryImpl.kt`
- Create: `app/src/main/kotlin/com/wisdometer/di/DatabaseModule.kt`
- Create: `app/src/main/kotlin/com/wisdometer/di/AppModule.kt`

- [ ] **Step 1: Create PredictionRepository.kt (interface)**

```kotlin
// app/src/main/kotlin/com/wisdometer/data/repository/PredictionRepository.kt
package com.wisdometer.data.repository

import com.wisdometer.data.model.Prediction
import com.wisdometer.data.model.PredictionOption
import com.wisdometer.data.model.PredictionWithOptions
import kotlinx.coroutines.flow.Flow

interface PredictionRepository {
    fun getAllPredictions(): Flow<List<PredictionWithOptions>>
    fun getPredictionById(id: Long): Flow<PredictionWithOptions?>
    suspend fun savePrediction(prediction: Prediction, options: List<PredictionOption>)
    suspend fun deletePrediction(prediction: Prediction)
    suspend fun getAllResolved(): List<PredictionWithOptions>
    suspend fun importPredictions(items: List<PredictionWithOptions>)
}
```

- [ ] **Step 2: Create PredictionRepositoryImpl.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/data/repository/PredictionRepositoryImpl.kt
package com.wisdometer.data.repository

import com.wisdometer.data.dao.PredictionDao
import com.wisdometer.data.model.Prediction
import com.wisdometer.data.model.PredictionOption
import com.wisdometer.data.model.PredictionWithOptions
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject

class PredictionRepositoryImpl @Inject constructor(
    private val dao: PredictionDao,
) : PredictionRepository {

    override fun getAllPredictions(): Flow<List<PredictionWithOptions>> =
        dao.getAllWithOptions(Instant.now().toEpochMilli())

    override fun getPredictionById(id: Long): Flow<PredictionWithOptions?> =
        dao.getWithOptionsById(id)

    override suspend fun savePrediction(prediction: Prediction, options: List<PredictionOption>) {
        dao.upsertPredictionWithOptions(prediction, options)
    }

    override suspend fun deletePrediction(prediction: Prediction) {
        dao.deletePrediction(prediction)
    }

    override suspend fun getAllResolved(): List<PredictionWithOptions> =
        dao.getAllResolvedWithOptions()

    override suspend fun importPredictions(items: List<PredictionWithOptions>) {
        for (item in items) {
            val existing = dao.countByQuestionAndCreatedAt(
                item.prediction.question,
                item.prediction.createdAt.toEpochMilli(),
            )
            if (existing == 0) {
                dao.upsertPredictionWithOptions(
                    item.prediction.copy(id = 0),
                    item.options.map { it.copy(id = 0, predictionId = 0) },
                )
            }
        }
    }
}
```

- [ ] **Step 3: Create DatabaseModule.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/di/DatabaseModule.kt
package com.wisdometer.di

import android.content.Context
import androidx.room.Room
import com.wisdometer.data.dao.PredictionDao
import com.wisdometer.data.db.WisdometerDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WisdometerDatabase =
        Room.databaseBuilder(context, WisdometerDatabase::class.java, "wisdometer.db").build()

    @Provides
    fun providePredictionDao(db: WisdometerDatabase): PredictionDao = db.predictionDao()
}
```

- [ ] **Step 4: Create AppModule.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/di/AppModule.kt
package com.wisdometer.di

import com.wisdometer.data.repository.PredictionRepository
import com.wisdometer.data.repository.PredictionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindPredictionRepository(impl: PredictionRepositoryImpl): PredictionRepository
}
```

- [ ] **Step 5: Verify compilation**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add app/src/main/kotlin/com/wisdometer/data/repository/ app/src/main/kotlin/com/wisdometer/di/
git commit -m "feat: add repository and Hilt DI modules"
```

---

## Task 5: Scoring Engine (TDD)

**Files:**
- Create: `app/src/main/kotlin/com/wisdometer/domain/ScoringEngine.kt`
- Create: `app/src/test/kotlin/com/wisdometer/domain/ScoringEngineTest.kt`

- [ ] **Step 1: Write the failing tests**

```kotlin
// app/src/test/kotlin/com/wisdometer/domain/ScoringEngineTest.kt
package com.wisdometer.domain

import com.wisdometer.data.model.Prediction
import com.wisdometer.data.model.PredictionOption
import com.wisdometer.data.model.PredictionWithOptions
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class ScoringEngineTest {

    private val engine = ScoringEngine()

    private fun makePrediction(
        options: List<Pair<String, Int>>,  // label to probability
        actualIndex: Int,
    ): PredictionWithOptions {
        val prediction = Prediction(
            id = 1L,
            question = "Q",
            createdAt = Instant.EPOCH,
            resolvedAt = Instant.EPOCH,
            outcomeOptionId = (actualIndex + 1).toLong(),
        )
        val predictionOptions = options.mapIndexed { i, (label, prob) ->
            PredictionOption(
                id = (i + 1).toLong(),
                predictionId = 1L,
                label = label,
                probability = prob,
                sortOrder = i,
            )
        }
        return PredictionWithOptions(prediction, predictionOptions)
    }

    @Test
    fun `simpleCloseness is 1 when 100 percent on correct outcome`() {
        val p = makePrediction(listOf("Yes" to 100), actualIndex = 0)
        assertEquals(1.0, engine.simpleCloseness(listOf(p)), 0.001)
    }

    @Test
    fun `simpleCloseness is 0_1 when 10 percent on correct outcome`() {
        val p = makePrediction(listOf("Yes" to 10, "No" to 90), actualIndex = 0)
        assertEquals(0.1, engine.simpleCloseness(listOf(p)), 0.001)
    }

    @Test
    fun `simpleCloseness averages across multiple predictions`() {
        val p1 = makePrediction(listOf("Yes" to 100), actualIndex = 0)  // 1.0
        val p2 = makePrediction(listOf("Yes" to 0, "No" to 100), actualIndex = 0)  // 0.0
        assertEquals(0.5, engine.simpleCloseness(listOf(p1, p2)), 0.001)
    }

    @Test
    fun `brierScore is 0 for perfect prediction`() {
        // 100% on correct, 0% on others: (1-1)^2 + 0^2 = 0
        val p = makePrediction(listOf("Yes" to 100), actualIndex = 0)
        assertEquals(0.0, engine.brierScore(listOf(p)), 0.001)
    }

    @Test
    fun `brierScore is 2 for worst prediction single option`() {
        // 0% on correct (only option): (0-1)^2 = 1... but worst case for single option is 1
        // With two options: 0% correct, 100% wrong: (0-1)^2 + (1-0)^2 = 1 + 1 = 2
        val p = makePrediction(listOf("Yes" to 0, "No" to 100), actualIndex = 0)
        assertEquals(2.0, engine.brierScore(listOf(p)), 0.001)
    }

    @Test
    fun `brierScore is 0_5 for 50 percent on correct with two options`() {
        // (0.5-1)^2 + (0.5-0)^2 = 0.25 + 0.25 = 0.5
        val p = makePrediction(listOf("Yes" to 50, "No" to 50), actualIndex = 0)
        assertEquals(0.5, engine.brierScore(listOf(p)), 0.001)
    }

    @Test
    fun `simpleCloseness returns 0 for empty list`() {
        assertEquals(0.0, engine.simpleCloseness(emptyList()), 0.001)
    }

    @Test
    fun `brierScore returns 0 for empty list`() {
        assertEquals(0.0, engine.brierScore(emptyList()), 0.001)
    }

    @Test
    fun `simpleClosenessForTag filters by tag`() {
        val p1 = Prediction(id = 1, question = "Q1", createdAt = Instant.EPOCH, resolvedAt = Instant.EPOCH,
            outcomeOptionId = 1L, tags = "career")
        val p2 = Prediction(id = 2, question = "Q2", createdAt = Instant.EPOCH, resolvedAt = Instant.EPOCH,
            outcomeOptionId = 3L, tags = "finance")
        val opt1 = PredictionOption(id = 1, predictionId = 1, label = "Yes", probability = 100, sortOrder = 0)
        val opt2 = PredictionOption(id = 3, predictionId = 2, label = "No", probability = 10, sortOrder = 0)
        val items = listOf(
            PredictionWithOptions(p1, listOf(opt1)),
            PredictionWithOptions(p2, listOf(opt2)),
        )
        assertEquals(1.0, engine.simpleClosenessForTag(items, "career"), 0.001)
        assertEquals(0.1, engine.simpleClosenessForTag(items, "finance"), 0.001)
    }

    @Test
    fun `avgConfidence is mean probability of top-ranked option`() {
        val p1 = Prediction(id = 1, question = "Q", createdAt = Instant.EPOCH)
        val p2 = Prediction(id = 2, question = "Q2", createdAt = Instant.EPOCH)
        val opts1 = listOf(
            PredictionOption(id = 1, predictionId = 1, label = "Yes", probability = 80, sortOrder = 0),
            PredictionOption(id = 2, predictionId = 1, label = "No", probability = 20, sortOrder = 1),
        )
        val opts2 = listOf(
            PredictionOption(id = 3, predictionId = 2, label = "A", probability = 60, sortOrder = 0),
            PredictionOption(id = 4, predictionId = 2, label = "B", probability = 40, sortOrder = 1),
        )
        val items = listOf(PredictionWithOptions(p1, opts1), PredictionWithOptions(p2, opts2))
        // top ranked: 80 and 60, avg = 70
        assertEquals(70.0, engine.avgConfidence(items), 0.001)
    }

    @Test
    fun `accuracyOverTime returns cumulative rolling average by resolution date`() {
        val p1 = Prediction(id = 1, question = "Q1", createdAt = Instant.EPOCH,
            resolvedAt = Instant.ofEpochMilli(1000), outcomeOptionId = 1)
        val p2 = Prediction(id = 2, question = "Q2", createdAt = Instant.EPOCH,
            resolvedAt = Instant.ofEpochMilli(2000), outcomeOptionId = 3)
        val opt1 = PredictionOption(id = 1, predictionId = 1, label = "Y", probability = 100, sortOrder = 0)
        val opt2 = PredictionOption(id = 3, predictionId = 2, label = "N", probability = 50, sortOrder = 0)
        val items = listOf(PredictionWithOptions(p1, listOf(opt1)), PredictionWithOptions(p2, listOf(opt2)))
        val points = engine.accuracyOverTime(items)
        // point 0: resolvedAt=1000, cumulative = 1.0
        // point 1: resolvedAt=2000, cumulative = (1.0 + 0.5) / 2 = 0.75
        assertEquals(2, points.size)
        assertEquals(1.0, points[0].second, 0.001)
        assertEquals(0.75, points[1].second, 0.001)
    }

    @Test
    fun `accuracyOverCount returns cumulative rolling average by prediction count`() {
        val p1 = Prediction(id = 1, question = "Q1", createdAt = Instant.EPOCH,
            resolvedAt = Instant.ofEpochMilli(1000), outcomeOptionId = 1)
        val p2 = Prediction(id = 2, question = "Q2", createdAt = Instant.EPOCH,
            resolvedAt = Instant.ofEpochMilli(2000), outcomeOptionId = 3)
        val opt1 = PredictionOption(id = 1, predictionId = 1, label = "Y", probability = 100, sortOrder = 0)
        val opt2 = PredictionOption(id = 3, predictionId = 2, label = "N", probability = 50, sortOrder = 0)
        val items = listOf(PredictionWithOptions(p1, listOf(opt1)), PredictionWithOptions(p2, listOf(opt2)))
        val points = engine.accuracyOverCount(items)
        assertEquals(2, points.size)
        assertEquals(1, points[0].first)
        assertEquals(1.0, points[0].second, 0.001)
        assertEquals(2, points[1].first)
        assertEquals(0.75, points[1].second, 0.001)
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew test --tests "com.wisdometer.domain.ScoringEngineTest"`
Expected: FAILED — `ScoringEngine` class not found

- [ ] **Step 3: Write ScoringEngine.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/domain/ScoringEngine.kt
package com.wisdometer.domain

import com.wisdometer.data.model.PredictionWithOptions
import com.wisdometer.data.model.tagList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScoringEngine @Inject constructor() {

    /** Score = probability_of_actual_outcome / 100, averaged across resolved predictions. */
    fun simpleCloseness(resolved: List<PredictionWithOptions>): Double {
        if (resolved.isEmpty()) return 0.0
        return resolved.map { item ->
            val actual = item.actualOption ?: return@map 0.0
            actual.probability / 100.0
        }.average()
    }

    /** Brier score = (p_actual - 1)² + Σ(p_other)², averaged across resolved predictions.
     *  0.0 = perfect, 2.0 = worst. */
    fun brierScore(resolved: List<PredictionWithOptions>): Double {
        if (resolved.isEmpty()) return 0.0
        return resolved.map { item ->
            val actual = item.actualOption ?: return@map 2.0
            val pActual = actual.probability / 100.0
            val sumOthersSq = item.options
                .filter { it.id != actual.id }
                .sumOf { (it.probability / 100.0).let { p -> p * p } }
            (pActual - 1.0).let { it * it } + sumOthersSq
        }.average()
    }

    fun simpleClosenessForTag(items: List<PredictionWithOptions>, tag: String): Double {
        val resolved = items.filter { it.isResolved && tag in it.prediction.tagList }
        return simpleCloseness(resolved)
    }

    fun brierScoreForTag(items: List<PredictionWithOptions>, tag: String): Double {
        val resolved = items.filter { it.isResolved && tag in it.prediction.tagList }
        return brierScore(resolved)
    }

    /** Mean probability of the top-ranked option across ALL predictions (resolved or not). */
    fun avgConfidence(items: List<PredictionWithOptions>): Double {
        if (items.isEmpty()) return 0.0
        return items.map { item ->
            item.options.maxOfOrNull { it.probability }?.toDouble() ?: 0.0
        }.average()
    }

    /** Returns list of (resolvedAtEpochMs, cumulativeAccuracy) sorted by resolution time. */
    fun accuracyOverTime(resolved: List<PredictionWithOptions>): List<Pair<Long, Double>> {
        val sorted = resolved
            .filter { it.isResolved }
            .sortedBy { it.prediction.resolvedAt!!.toEpochMilli() }
        return cumulativeAccuracy(sorted).mapIndexed { i, acc ->
            Pair(sorted[i].prediction.resolvedAt!!.toEpochMilli(), acc)
        }
    }

    /** Returns list of (count, cumulativeAccuracy) sorted by resolution time. */
    fun accuracyOverCount(resolved: List<PredictionWithOptions>): List<Pair<Int, Double>> {
        val sorted = resolved
            .filter { it.isResolved }
            .sortedBy { it.prediction.resolvedAt!!.toEpochMilli() }
        return cumulativeAccuracy(sorted).mapIndexed { i, acc -> Pair(i + 1, acc) }
    }

    private fun cumulativeAccuracy(sortedResolved: List<PredictionWithOptions>): List<Double> {
        var runningSum = 0.0
        return sortedResolved.mapIndexed { i, item ->
            val score = item.actualOption?.probability?.div(100.0) ?: 0.0
            runningSum += score
            runningSum / (i + 1)
        }
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew test --tests "com.wisdometer.domain.ScoringEngineTest"`
Expected: BUILD SUCCESSFUL, all tests pass

- [ ] **Step 5: Commit**

```bash
git add app/src/main/kotlin/com/wisdometer/domain/ app/src/test/kotlin/com/wisdometer/domain/
git commit -m "feat: add scoring engine with tests"
```

---

## Task 6: Theme & Base UI Components

**Files:**
- Create: `app/src/main/kotlin/com/wisdometer/ui/theme/Color.kt`
- Create: `app/src/main/kotlin/com/wisdometer/ui/theme/Type.kt`
- Create: `app/src/main/kotlin/com/wisdometer/ui/theme/Theme.kt`
- Create: `app/src/main/kotlin/com/wisdometer/ui/components/ProbabilityBar.kt`
- Create: `app/src/main/kotlin/com/wisdometer/ui/components/StatusBadge.kt`
- Create: `app/src/main/kotlin/com/wisdometer/ui/components/PredictionCard.kt`

- [ ] **Step 1: Create Color.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/ui/theme/Color.kt
package com.wisdometer.ui.theme

import androidx.compose.ui.graphics.Color

val Background = Color(0xFFFAFAF8)
val CardBackground = Color(0xFFFFFFFF)
val CardShadow = Color(0x1A000000)
val PrimaryText = Color(0xFF1A1A1A)
val SecondaryText = Color(0xFF6B6B6B)
val DividerColor = Color(0xFFE8E8E4)

// Status badge colors
val BadgeOpenBackground = Color(0xFFFFF3CD)
val BadgeOpenText = Color(0xFF856404)
val BadgeResolvedBackground = Color(0xFFD4EDDA)
val BadgeResolvedText = Color(0xFF155724)

// Probability bar colors (cycle through these for options)
val BarColors = listOf(
    Color(0xFF4A90D9),
    Color(0xFF7EC8A4),
    Color(0xFFE8A44A),
    Color(0xFFD96A6A),
    Color(0xFF9B7EC8),
    Color(0xFF4AC8C8),
)
```

- [ ] **Step 2: Create Type.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/ui/theme/Type.kt
package com.wisdometer.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val WisdometerTypography = Typography(
    headlineLarge = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PrimaryText),
    headlineMedium = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, color = PrimaryText),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = PrimaryText),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, color = SecondaryText),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, color = SecondaryText),
)
```

- [ ] **Step 3: Create Theme.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/ui/theme/Theme.kt
package com.wisdometer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    background = Background,
    surface = CardBackground,
    onBackground = PrimaryText,
    onSurface = PrimaryText,
)

@Composable
fun WisdometerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = WisdometerTypography,
        content = content,
    )
}
```

- [ ] **Step 4: Create ProbabilityBar.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/ui/components/ProbabilityBar.kt
package com.wisdometer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisdometer.ui.theme.SecondaryText

@Composable
fun ProbabilityBar(
    label: String,
    probability: Int,
    barColor: Color,
    isActualOutcome: Boolean,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    val barHeight: Dp = if (compact) 6.dp else 10.dp
    val verticalPadding: Dp = if (compact) 2.dp else 4.dp

    Column(modifier = modifier.padding(vertical = verticalPadding)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (isActualOutcome) "✓ $label" else label,
                fontSize = if (compact) 11.sp else 13.sp,
                color = if (isActualOutcome) barColor else SecondaryText,
            )
            Text(
                text = "$probability%",
                fontSize = if (compact) 11.sp else 13.sp,
                color = SecondaryText,
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(RoundedCornerShape(50))
                .background(barColor.copy(alpha = 0.15f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = probability / 100f)
                    .clip(RoundedCornerShape(50))
                    .background(barColor),
            )
        }
    }
}
```

- [ ] **Step 5: Create StatusBadge.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/ui/components/StatusBadge.kt
package com.wisdometer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisdometer.ui.theme.*

@Composable
fun StatusBadge(isResolved: Boolean, modifier: Modifier = Modifier) {
    val bg = if (isResolved) BadgeResolvedBackground else BadgeOpenBackground
    val text = if (isResolved) BadgeResolvedText else BadgeOpenText
    val label = if (isResolved) "RESOLVED" else "OPEN"

    Text(
        text = label,
        fontSize = 10.sp,
        color = text,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}
```

- [ ] **Step 6: Create PredictionCard.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/ui/components/PredictionCard.kt
package com.wisdometer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wisdometer.data.model.PredictionWithOptions
import com.wisdometer.data.model.tagList
import com.wisdometer.ui.theme.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneId.systemDefault())

@Composable
fun PredictionCard(
    item: PredictionWithOptions,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardPadding = if (compact) 10.dp else 16.dp
    val optionSpacing = if (compact) 2.dp else 4.dp

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(cardPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = item.prediction.question,
                    style = WisdometerTypography.titleMedium,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                )
                StatusBadge(isResolved = item.isResolved)
            }

            Spacer(modifier = Modifier.height(optionSpacing + 4.dp))

            item.sortedOptions.forEachIndexed { index, option ->
                ProbabilityBar(
                    label = option.label,
                    probability = option.probability,
                    barColor = BarColors[index % BarColors.size],
                    isActualOutcome = option.id == item.prediction.outcomeOptionId,
                    compact = compact,
                )
            }

            val tags = item.prediction.tagList
            val reminder = item.prediction.reminderAt

            if (tags.isNotEmpty() || reminder != null) {
                Spacer(modifier = Modifier.height(if (compact) 4.dp else 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = tags.joinToString(" · "),
                        style = WisdometerTypography.bodySmall,
                    )
                    if (reminder != null) {
                        Text(
                            text = "⏰ ${dateFormatter.format(reminder)}",
                            style = WisdometerTypography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 7: Verify compilation**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git add app/src/main/kotlin/com/wisdometer/ui/
git commit -m "feat: add theme and base UI components"
```

---

## Task 7: Navigation & NavGraph

**Files:**
- Create: `app/src/main/kotlin/com/wisdometer/ui/navigation/NavGraph.kt`

- [ ] **Step 1: Create NavGraph.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/ui/navigation/NavGraph.kt
package com.wisdometer.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.*
import com.wisdometer.ui.detail.PredictionDetailScreen
import com.wisdometer.ui.edit.EditPredictionScreen
import com.wisdometer.ui.predictions.PredictionsScreen
import com.wisdometer.ui.profile.ProfileScreen
import com.wisdometer.ui.settings.SettingsScreen
import com.wisdometer.ui.theme.Background

sealed class Route(val path: String) {
    object Predictions : Route("predictions")
    object Profile : Route("profile")
    object Settings : Route("settings")
    object Detail : Route("detail/{predictionId}") {
        fun withId(id: Long) = "detail/$id"
    }
    object Edit : Route("edit?predictionId={predictionId}") {
        fun newPrediction() = "edit"
        fun editExisting(id: Long) = "edit?predictionId=$id"
    }
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    val bottomRoutes = listOf(Route.Predictions.path, Route.Profile.path, Route.Settings.path)

    Scaffold(
        containerColor = Background,
        bottomBar = {
            if (currentRoute in bottomRoutes) {
                NavigationBar(containerColor = Background) {
                    NavigationBarItem(
                        selected = currentRoute == Route.Predictions.path,
                        onClick = { navController.navigate(Route.Predictions.path) { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.List, contentDescription = "Predictions") },
                        label = { Text("Predictions") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == Route.Profile.path,
                        onClick = { navController.navigate(Route.Profile.path) { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.BarChart, contentDescription = "Profile") },
                        label = { Text("Profile") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == Route.Settings.path,
                        onClick = { navController.navigate(Route.Settings.path) { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Route.Predictions.path,
            modifier = Modifier.padding(padding),
        ) {
            composable(Route.Predictions.path) {
                PredictionsScreen(
                    onNavigateToDetail = { id -> navController.navigate(Route.Detail.withId(id)) },
                    onNavigateToNew = { navController.navigate(Route.Edit.newPrediction()) },
                )
            }
            composable(Route.Profile.path) {
                ProfileScreen()
            }
            composable(Route.Settings.path) {
                SettingsScreen()
            }
            composable(
                route = Route.Detail.path,
                arguments = listOf(navArgument("predictionId") { type = NavType.LongType }),
            ) { backStack ->
                val id = backStack.arguments!!.getLong("predictionId")
                PredictionDetailScreen(
                    predictionId = id,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Route.Edit.editExisting(id)) },
                )
            }
            composable(
                route = Route.Edit.path,
                arguments = listOf(
                    navArgument("predictionId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                ),
            ) { backStack ->
                val id = backStack.arguments!!.getLong("predictionId").takeIf { it != -1L }
                EditPredictionScreen(
                    predictionId = id,
                    onDone = { navController.popBackStack() },
                )
            }
        }
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL (may have stub composable errors — stub them in next tasks)

Note: If compilation fails because referenced screens don't exist yet, create minimal stub composables in each file — e.g.:
```kotlin
@Composable fun PredictionsScreen(onNavigateToDetail: (Long) -> Unit, onNavigateToNew: () -> Unit) {}
```
Add the stubs to their correct file paths so later tasks can fill them in.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/kotlin/com/wisdometer/ui/navigation/
git commit -m "feat: add navigation graph"
```

---

## Task 8: Predictions List Screen

**Files:**
- Create: `app/src/main/kotlin/com/wisdometer/ui/predictions/PredictionsViewModel.kt`
- Create: `app/src/main/kotlin/com/wisdometer/ui/predictions/PredictionsScreen.kt`

- [ ] **Step 1: Create PredictionsViewModel.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/ui/predictions/PredictionsViewModel.kt
package com.wisdometer.ui.predictions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisdometer.data.model.PredictionWithOptions
import com.wisdometer.data.model.tagList
import com.wisdometer.data.repository.PredictionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

enum class StatusFilter { ALL, OPEN, RESOLVED }

data class PredictionsUiState(
    val items: List<PredictionWithOptions> = emptyList(),
    val statusFilter: StatusFilter = StatusFilter.ALL,
    val selectedTag: String? = null,
    val availableTags: List<String> = emptyList(),
    val compact: Boolean = false,
)

@HiltViewModel
class PredictionsViewModel @Inject constructor(
    private val repository: PredictionRepository,
) : ViewModel() {

    private val _statusFilter = MutableStateFlow(StatusFilter.ALL)
    private val _selectedTag = MutableStateFlow<String?>(null)
    private val _compact = MutableStateFlow(false)

    val uiState: StateFlow<PredictionsUiState> = combine(
        repository.getAllPredictions(),
        _statusFilter,
        _selectedTag,
        _compact,
    ) { all, statusFilter, selectedTag, compact ->
        val filtered = all
            .filter { item ->
                when (statusFilter) {
                    StatusFilter.ALL -> true
                    StatusFilter.OPEN -> !item.isResolved
                    StatusFilter.RESOLVED -> item.isResolved
                }
            }
            .filter { item ->
                selectedTag == null || selectedTag in item.prediction.tagList
            }
        val tags = all.flatMap { it.prediction.tagList }.distinct().sorted()
        PredictionsUiState(filtered, statusFilter, selectedTag, tags, compact)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PredictionsUiState())

    fun setStatusFilter(filter: StatusFilter) { _statusFilter.value = filter }
    fun setTagFilter(tag: String?) { _selectedTag.value = tag }
    fun setCompact(compact: Boolean) { _compact.value = compact }
}
```

- [ ] **Step 2: Create PredictionsScreen.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/ui/predictions/PredictionsScreen.kt
package com.wisdometer.ui.predictions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wisdometer.ui.components.PredictionCard
import com.wisdometer.ui.theme.Background
import com.wisdometer.ui.theme.WisdometerTypography

@Composable
fun PredictionsScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToNew: () -> Unit,
    viewModel: PredictionsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Background,
        topBar = {
            Text(
                "Wisdometer",
                style = WisdometerTypography.headlineLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToNew) {
                Icon(Icons.Default.Add, contentDescription = "New prediction")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Status filter row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(StatusFilter.values().toList()) { filter ->
                    FilterChip(
                        selected = state.statusFilter == filter,
                        onClick = { viewModel.setStatusFilter(filter) },
                        label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    )
                }
                items(state.availableTags) { tag ->
                    FilterChip(
                        selected = state.selectedTag == tag,
                        onClick = {
                            viewModel.setTagFilter(if (state.selectedTag == tag) null else tag)
                        },
                        label = { Text(tag) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.items, key = { it.prediction.id }) { item ->
                    PredictionCard(
                        item = item,
                        compact = state.compact,
                        onClick = { onNavigateToDetail(item.prediction.id) },
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 3: Verify compilation**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/kotlin/com/wisdometer/ui/predictions/
git commit -m "feat: predictions list screen with filter bar"
```

---

## Task 9: New / Edit Prediction Screen

**Files:**
- Create: `app/src/main/kotlin/com/wisdometer/ui/edit/EditPredictionViewModel.kt`
- Create: `app/src/main/kotlin/com/wisdometer/ui/edit/EditPredictionScreen.kt`

- [ ] **Step 1: Create EditPredictionViewModel.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/ui/edit/EditPredictionViewModel.kt
package com.wisdometer.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisdometer.data.model.Prediction
import com.wisdometer.data.model.PredictionOption
import com.wisdometer.data.repository.PredictionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class OptionDraft(
    val id: Long = 0,
    val label: String = "",
    val probability: Int = 0,
    val sortOrder: Int = 0,
)

data class EditUiState(
    val question: String = "",
    val options: List<OptionDraft> = listOf(OptionDraft(sortOrder = 0), OptionDraft(sortOrder = 1)),
    val reminderAt: Instant? = null,
    val tagsInput: String = "",
    val probabilitySum: Int = 0,
    val isSaving: Boolean = false,
    val isLoaded: Boolean = false,
)

@HiltViewModel
class EditPredictionViewModel @Inject constructor(
    private val repository: PredictionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(EditUiState())
    val state: StateFlow<EditUiState> = _state.asStateFlow()

    private var editingPredictionId: Long? = null

    fun loadPrediction(id: Long) {
        if (_state.value.isLoaded) return
        viewModelScope.launch {
            repository.getPredictionById(id).firstOrNull()?.let { item ->
                editingPredictionId = id
                _state.update {
                    it.copy(
                        question = item.prediction.question,
                        options = item.sortedOptions.mapIndexed { i, opt ->
                            OptionDraft(opt.id, opt.label, opt.probability, i)
                        },
                        reminderAt = item.prediction.reminderAt,
                        tagsInput = item.prediction.tags.replace(",", ", "),
                        probabilitySum = item.options.sumOf { opt -> opt.probability },
                        isLoaded = true,
                    )
                }
            }
        }
    }

    fun setQuestion(q: String) = _state.update { it.copy(question = q) }
    fun setReminder(instant: Instant?) = _state.update { it.copy(reminderAt = instant) }
    fun setTagsInput(t: String) = _state.update { it.copy(tagsInput = t) }

    fun setOptionLabel(index: Int, label: String) = updateOption(index) { it.copy(label = label) }
    fun setOptionProbability(index: Int, prob: Int) {
        updateOption(index) { it.copy(probability = prob.coerceIn(0, 100)) }
        recalcSum()
    }

    fun addOption() = _state.update { s ->
        val newOptions = s.options + OptionDraft(sortOrder = s.options.size)
        s.copy(options = newOptions)
    }

    fun removeOption(index: Int) = _state.update { s ->
        val newOptions = s.options.toMutableList().also { it.removeAt(index) }
            .mapIndexed { i, opt -> opt.copy(sortOrder = i) }
        s.copy(options = newOptions, probabilitySum = newOptions.sumOf { it.probability })
    }

    private fun updateOption(index: Int, transform: (OptionDraft) -> OptionDraft) =
        _state.update { s ->
            val newOptions = s.options.toMutableList()
            newOptions[index] = transform(newOptions[index])
            s.copy(options = newOptions)
        }

    private fun recalcSum() = _state.update { s ->
        s.copy(probabilitySum = s.options.sumOf { it.probability })
    }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        if (s.question.isBlank() || s.probabilitySum != 100) return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val tags = s.tagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }.joinToString(",")
            val prediction = Prediction(
                id = editingPredictionId ?: 0L,
                question = s.question.trim(),
                createdAt = Instant.now(),
                reminderAt = s.reminderAt,
                tags = tags,
            )
            val options = s.options.mapIndexed { i, draft ->
                PredictionOption(
                    id = draft.id,
                    predictionId = editingPredictionId ?: 0L,
                    label = draft.label,
                    probability = draft.probability,
                    sortOrder = i,
                )
            }
            repository.savePrediction(prediction, options)
            onDone()
        }
    }
}
```

- [ ] **Step 2: Create EditPredictionScreen.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/ui/edit/EditPredictionScreen.kt
package com.wisdometer.ui.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wisdometer.ui.theme.WisdometerTypography

@Composable
fun EditPredictionScreen(
    predictionId: Long?,
    onDone: () -> Unit,
    viewModel: EditPredictionViewModel = hiltViewModel(),
) {
    LaunchedEffect(predictionId) {
        predictionId?.let { viewModel.loadPrediction(it) }
    }

    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (predictionId == null) "New Prediction" else "Edit Prediction",
                    style = WisdometerTypography.headlineMedium,
                )
                IconButton(onClick = onDone) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
        ) {
            OutlinedTextField(
                value = state.question,
                onValueChange = viewModel::setQuestion,
                label = { Text("Question") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Options", style = WisdometerTypography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            state.options.forEachIndexed { index, option ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = option.label,
                        onValueChange = { viewModel.setOptionLabel(index, it) },
                        label = { Text("Label") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = if (option.probability == 0) "" else option.probability.toString(),
                        onValueChange = { v ->
                            viewModel.setOptionProbability(index, v.toIntOrNull() ?: 0)
                        },
                        label = { Text("%") },
                        modifier = Modifier.width(72.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    if (state.options.size > 2) {
                        IconButton(onClick = { viewModel.removeOption(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove option")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            val sumColor = if (state.probabilitySum == 100)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.error
            Text(
                "Sum: ${state.probabilitySum}% (must equal 100%)",
                style = WisdometerTypography.bodySmall,
                color = sumColor,
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = viewModel::addOption) {
                Text("+ Add option")
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = state.tagsInput,
                onValueChange = viewModel::setTagsInput,
                label = { Text("Tags (comma-separated)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { viewModel.save(onDone) },
                enabled = state.question.isNotBlank() && state.probabilitySum == 100 && !state.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (state.isSaving) "Saving…" else "Save")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
```

- [ ] **Step 3: Verify compilation**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/kotlin/com/wisdometer/ui/edit/
git commit -m "feat: new/edit prediction screen"
```

---

## Task 10: Prediction Detail Screen

**Files:**
- Create: `app/src/main/kotlin/com/wisdometer/ui/detail/PredictionDetailViewModel.kt`
- Create: `app/src/main/kotlin/com/wisdometer/ui/detail/PredictionDetailScreen.kt`

- [ ] **Step 1: Create PredictionDetailViewModel.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/ui/detail/PredictionDetailViewModel.kt
package com.wisdometer.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisdometer.data.model.PredictionWithOptions
import com.wisdometer.data.repository.PredictionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class PredictionDetailViewModel @Inject constructor(
    private val repository: PredictionRepository,
) : ViewModel() {

    private val _predictionId = MutableStateFlow<Long?>(null)

    val item: StateFlow<PredictionWithOptions?> = _predictionId
        .filterNotNull()
        .flatMapLatest { repository.getPredictionById(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun load(id: Long) { _predictionId.value = id }

    fun setOutcome(optionId: Long) {
        val current = item.value ?: return
        viewModelScope.launch {
            repository.savePrediction(
                current.prediction.copy(
                    outcomeOptionId = optionId,
                    resolvedAt = Instant.now(),
                ),
                current.options,
            )
        }
    }

    fun delete(onDone: () -> Unit) {
        val current = item.value ?: return
        viewModelScope.launch {
            repository.deletePrediction(current.prediction)
            onDone()
        }
    }
}
```

- [ ] **Step 2: Create PredictionDetailScreen.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/ui/detail/PredictionDetailScreen.kt
package com.wisdometer.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wisdometer.data.model.tagList
import com.wisdometer.ui.components.ProbabilityBar
import com.wisdometer.ui.components.StatusBadge
import com.wisdometer.ui.theme.BarColors
import com.wisdometer.ui.theme.WisdometerTypography

@Composable
fun PredictionDetailScreen(
    predictionId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    viewModel: PredictionDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(predictionId) { viewModel.load(predictionId) }

    val item by viewModel.item.collectAsState()
    var showOutcomeDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            }
        },
    ) { padding ->
        item?.let { pw ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        pw.prediction.question,
                        style = WisdometerTypography.headlineMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(isResolved = pw.isResolved)
                }
                Spacer(modifier = Modifier.height(16.dp))

                pw.sortedOptions.forEachIndexed { index, option ->
                    ProbabilityBar(
                        label = option.label,
                        probability = option.probability,
                        barColor = BarColors[index % BarColors.size],
                        isActualOutcome = option.id == pw.prediction.outcomeOptionId,
                        compact = false,
                    )
                }

                val tags = pw.prediction.tagList
                if (tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Tags: ${tags.joinToString(", ")}", style = WisdometerTypography.bodySmall)
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (!pw.isResolved) {
                    Button(
                        onClick = { showOutcomeDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Set Outcome")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Delete Prediction")
                }
            }

            if (showOutcomeDialog) {
                AlertDialog(
                    onDismissRequest = { showOutcomeDialog = false },
                    title = { Text("Select Outcome") },
                    text = {
                        Column {
                            pw.sortedOptions.forEach { option ->
                                TextButton(
                                    onClick = {
                                        viewModel.setOutcome(option.id)
                                        showOutcomeDialog = false
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(option.label)
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showOutcomeDialog = false }) { Text("Cancel") }
                    },
                )
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Prediction?") },
                    text = { Text("This cannot be undone.") },
                    confirmButton = {
                        TextButton(onClick = { viewModel.delete(onBack) }) { Text("Delete") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                    },
                )
            }
        } ?: Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}
```

- [ ] **Step 3: Verify compilation**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/kotlin/com/wisdometer/ui/detail/
git commit -m "feat: prediction detail screen"
```

---

## Task 11: Profile Screen

**Files:**
- Create: `app/src/main/kotlin/com/wisdometer/ui/profile/ProfileViewModel.kt`
- Create: `app/src/main/kotlin/com/wisdometer/ui/profile/AccuracyChart.kt`
- Create: `app/src/main/kotlin/com/wisdometer/ui/profile/ProfileScreen.kt`

- [ ] **Step 1: Create ProfileViewModel.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/ui/profile/ProfileViewModel.kt
package com.wisdometer.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisdometer.data.model.PredictionWithOptions
import com.wisdometer.data.model.tagList
import com.wisdometer.data.repository.PredictionRepository
import com.wisdometer.domain.ScoringEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class TagAccuracy(val tag: String, val closeness: Double, val brierScore: Double)

data class ProfileUiState(
    val totalPredictions: Int = 0,
    val resolvedPredictions: Int = 0,
    val openPredictions: Int = 0,
    val avgConfidence: Double = 0.0,
    val simpleCloseness: Double = 0.0,
    val brierScore: Double = 0.0,
    val tagAccuracies: List<TagAccuracy> = emptyList(),
    val accuracyOverTime: List<Pair<Long, Double>> = emptyList(),
    val accuracyOverCount: List<Pair<Int, Double>> = emptyList(),
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: PredictionRepository,
    private val engine: ScoringEngine,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = repository.getAllPredictions()
        .map { all ->
            val resolved = all.filter { it.isResolved }
            val tags = all.flatMap { it.prediction.tagList }.distinct()
            val tagAccuracies = tags.map { tag ->
                TagAccuracy(
                    tag = tag,
                    closeness = engine.simpleClosenessForTag(all, tag),
                    brierScore = engine.brierScoreForTag(all, tag),
                )
            }
            ProfileUiState(
                totalPredictions = all.size,
                resolvedPredictions = resolved.size,
                openPredictions = all.size - resolved.size,
                avgConfidence = engine.avgConfidence(all),
                simpleCloseness = engine.simpleCloseness(resolved),
                brierScore = engine.brierScore(resolved),
                tagAccuracies = tagAccuracies,
                accuracyOverTime = engine.accuracyOverTime(resolved),
                accuracyOverCount = engine.accuracyOverCount(resolved),
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())
}
```

- [ ] **Step 2: Create AccuracyChart.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/ui/profile/AccuracyChart.kt
package com.wisdometer.ui.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisdometer.ui.theme.BarColors

@Composable
fun AccuracyChart(
    points: List<Pair<*, Double>>,  // x can be Long (epoch ms) or Int (count)
    modifier: Modifier = Modifier,
) {
    if (points.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No resolved predictions yet", fontSize = 12.sp, color = Color.Gray)
        }
        return
    }

    val lineColor = BarColors[0]
    val gridColor = Color(0xFFE0E0E0)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val padH = 24f
        val padV = 16f
        val chartW = w - 2 * padH
        val chartH = h - 2 * padV

        // Draw grid lines at 0%, 25%, 50%, 75%, 100%
        for (pct in listOf(0.0, 0.25, 0.5, 0.75, 1.0)) {
            val y = padV + chartH * (1.0 - pct).toFloat()
            drawLine(gridColor, Offset(padH, y), Offset(padH + chartW, y), strokeWidth = 1f)
        }

        // Plot line
        val path = Path()
        points.forEachIndexed { i, (_, accuracy) ->
            val x = padH + chartW * (i.toFloat() / (points.size - 1).coerceAtLeast(1))
            val y = padV + chartH * (1.0 - accuracy).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, lineColor, style = Stroke(width = 3f))

        // Plot dots
        points.forEachIndexed { i, (_, accuracy) ->
            val x = padH + chartW * (i.toFloat() / (points.size - 1).coerceAtLeast(1))
            val y = padV + chartH * (1.0 - accuracy).toFloat()
            drawCircle(lineColor, radius = 5f, center = Offset(x, y))
        }
    }
}
```

- [ ] **Step 3: Create ProfileScreen.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/ui/profile/ProfileScreen.kt
package com.wisdometer.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wisdometer.ui.theme.WisdometerTypography
import kotlin.math.roundToInt

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var useTimeAxis by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text("Profile", style = WisdometerTypography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // Overall accuracy
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "${(state.simpleCloseness * 100).roundToInt()}% Accuracy",
                    style = WisdometerTypography.headlineLarge,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Brier Score: ${"%.2f".format(state.brierScore)}", style = WisdometerTypography.bodySmall)
                    var showTooltip by remember { mutableStateOf(false) }
                    TextButton(onClick = { showTooltip = !showTooltip }, contentPadding = PaddingValues(0.dp)) {
                        Text("?", fontSize = 12.sp)
                    }
                    if (showTooltip) {
                        AlertDialog(
                            onDismissRequest = { showTooltip = false },
                            title = { Text("Brier Score") },
                            text = { Text("Brier score measures calibration — 0.0 is perfect, 2.0 is worst.") },
                            confirmButton = { TextButton(onClick = { showTooltip = false }) { Text("OK") } },
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary stats
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Summary", style = WisdometerTypography.titleMedium)
                Text("Total: ${state.totalPredictions}")
                Text("Resolved: ${state.resolvedPredictions}")
                Text("Open: ${state.openPredictions}")
                Text("Avg Confidence: ${(state.avgConfidence).roundToInt()}%")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Accuracy", style = WisdometerTypography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = useTimeAxis,
                        onClick = { useTimeAxis = true },
                        label = { Text("Over time") },
                    )
                    FilterChip(
                        selected = !useTimeAxis,
                        onClick = { useTimeAxis = false },
                        label = { Text("By count") },
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                val chartPoints = if (useTimeAxis) state.accuracyOverTime else state.accuracyOverCount
                AccuracyChart(
                    points = chartPoints,
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                )
            }
        }

        // Tag breakdown
        if (state.tagAccuracies.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("By Tag", style = WisdometerTypography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    state.tagAccuracies.forEach { ta ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(ta.tag, style = WisdometerTypography.bodyMedium)
                            Text("${(ta.closeness * 100).roundToInt()}%", style = WisdometerTypography.bodySmall)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
```

- [ ] **Step 4: Verify compilation**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/main/kotlin/com/wisdometer/ui/profile/
git commit -m "feat: profile screen with accuracy chart and tag breakdown"
```

---

## Task 12: Settings Screen

**Files:**
- Create: `app/src/main/kotlin/com/wisdometer/ui/settings/SettingsViewModel.kt`
- Create: `app/src/main/kotlin/com/wisdometer/ui/settings/SettingsScreen.kt`

Note: This task references `JsonExporter`, `JsonImporter`, and `NotificationScheduler` which are created in Tasks 13 and 14. Wire them in after those tasks complete, or add TODO stubs now and fill in after.

- [ ] **Step 1: Create SettingsViewModel.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/ui/settings/SettingsViewModel.kt
package com.wisdometer.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisdometer.export.JsonExporter
import com.wisdometer.export.JsonImporter
import com.wisdometer.notifications.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PREFS_NAME = "wisdometer_settings"
private const val KEY_COMPACT = "compact_mode"
private const val KEY_NOTIFICATIONS = "notifications_enabled"

data class SettingsUiState(
    val compact: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val statusMessage: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exporter: JsonExporter,
    private val importer: JsonImporter,
    private val notificationScheduler: NotificationScheduler,
) : ViewModel() {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(
        SettingsUiState(
            compact = prefs.getBoolean(KEY_COMPACT, false),
            notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS, true),
        )
    )
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    fun setCompact(value: Boolean) {
        prefs.edit().putBoolean(KEY_COMPACT, value).apply()
        _state.update { it.copy(compact = value) }
    }

    fun setNotificationsEnabled(value: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS, value).apply()
        _state.update { it.copy(notificationsEnabled = value) }
        if (!value) notificationScheduler.cancelAll()
    }

    fun exportToUri(uri: Uri) {
        viewModelScope.launch {
            try {
                exporter.exportToUri(uri)
                _state.update { it.copy(statusMessage = "Export successful") }
            } catch (e: Exception) {
                _state.update { it.copy(statusMessage = "Export failed: ${e.message}") }
            }
        }
    }

    fun importFromUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val count = importer.importFromUri(uri)
                _state.update { it.copy(statusMessage = "Imported $count predictions") }
            } catch (e: Exception) {
                _state.update { it.copy(statusMessage = "Import failed: ${e.message}") }
            }
        }
    }

    fun clearStatusMessage() = _state.update { it.copy(statusMessage = null) }
}
```

- [ ] **Step 2: Create SettingsScreen.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/ui/settings/SettingsScreen.kt
package com.wisdometer.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wisdometer.ui.theme.WisdometerTypography
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? -> uri?.let { viewModel.exportToUri(it) } }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> uri?.let { viewModel.importFromUri(it) } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text("Settings", style = WisdometerTypography.headlineLarge)
        Spacer(modifier = Modifier.height(24.dp))

        // Compact mode
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Compact mode", style = WisdometerTypography.bodyMedium)
            Switch(checked = state.compact, onCheckedChange = viewModel::setCompact)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Notifications
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Notifications", style = WisdometerTypography.bodyMedium)
            Switch(checked = state.notificationsEnabled, onCheckedChange = viewModel::setNotificationsEnabled)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Export
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        OutlinedButton(
            onClick = { exportLauncher.launch("wisdometer-export-$today.json") },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Export JSON")
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Import
        OutlinedButton(
            onClick = { importLauncher.launch(arrayOf("application/json")) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Import JSON")
        }

        state.statusMessage?.let { msg ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(msg, style = WisdometerTypography.bodySmall, color = MaterialTheme.colorScheme.primary)
            LaunchedEffect(msg) {
                kotlinx.coroutines.delay(3000)
                viewModel.clearStatusMessage()
            }
        }
    }
}
```

- [ ] **Step 3: Note on PredictionsScreen compact mode integration**

The compact mode setting is in SharedPreferences. `PredictionsViewModel` should read it. Update `PredictionsViewModel` to load compact from SharedPreferences on init:

```kotlin
// Add to PredictionsViewModel constructor:
@ApplicationContext private val context: Context,

// In init block, read SharedPreferences:
init {
    val prefs = context.getSharedPreferences("wisdometer_settings", Context.MODE_PRIVATE)
    _compact.value = prefs.getBoolean("compact_mode", false)
}
```

And add `@ApplicationContext private val context: Context,` to the constructor. This keeps compact mode in sync when navigating to Predictions after changing it in Settings.

- [ ] **Step 4: Verify compilation**

Run: `./gradlew compileDebugKotlin`
Note: Will fail until JsonExporter, JsonImporter, NotificationScheduler exist. Create stub classes:

```kotlin
// Stub: app/src/main/kotlin/com/wisdometer/export/JsonExporter.kt
package com.wisdometer.export
import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class JsonExporter @Inject constructor() {
    suspend fun exportToUri(uri: Uri): Unit = TODO("implement in Task 14")
}

// Stub: app/src/main/kotlin/com/wisdometer/export/JsonImporter.kt
package com.wisdometer.export
import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class JsonImporter @Inject constructor() {
    suspend fun importFromUri(uri: Uri): Int = TODO("implement in Task 14")
}

// Stub: app/src/main/kotlin/com/wisdometer/notifications/NotificationScheduler.kt
package com.wisdometer.notifications
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class NotificationScheduler @Inject constructor() {
    fun cancelAll() = Unit
    fun schedule(predictionId: Long, reminderAtMs: Long, question: String) = Unit
}
```

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/main/kotlin/com/wisdometer/ui/settings/ app/src/main/kotlin/com/wisdometer/export/ app/src/main/kotlin/com/wisdometer/notifications/
git commit -m "feat: settings screen with compact mode and notification toggle stubs"
```

---

## Task 13: WorkManager Notifications

**Files:**
- Modify: `app/src/main/kotlin/com/wisdometer/notifications/NotificationScheduler.kt` (replace stub)
- Create: `app/src/main/kotlin/com/wisdometer/notifications/ReminderWorker.kt`

Also wire up reminder scheduling when a prediction with `reminderAt` is saved. Modify `PredictionRepositoryImpl.savePrediction` to call `NotificationScheduler`.

- [ ] **Step 1: Create ReminderWorker.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/notifications/ReminderWorker.kt
package com.wisdometer.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wisdometer.MainActivity
import com.wisdometer.REMINDER_CHANNEL_ID
import com.wisdometer.data.dao.PredictionDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val dao: PredictionDao,
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_PREDICTION_ID = "prediction_id"
        const val KEY_QUESTION = "question"
    }

    override suspend fun doWork(): Result {
        val predictionId = inputData.getLong(KEY_PREDICTION_ID, -1L)
        val question = inputData.getString(KEY_QUESTION) ?: return Result.failure()

        // Don't notify if already resolved
        val predictionWithOptions = dao.getAllResolvedWithOptions()
        if (predictionWithOptions.any { it.prediction.id == predictionId }) {
            return Result.success()
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("prediction_id", predictionId)
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            predictionId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(applicationContext, REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Time to resolve")
            .setContentText("\"$question\"")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(predictionId.toInt(), notification)
        return Result.success()
    }
}
```

- [ ] **Step 2: Replace NotificationScheduler.kt stub with full implementation**

```kotlin
// app/src/main/kotlin/com/wisdometer/notifications/NotificationScheduler.kt
package com.wisdometer.notifications

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val workManager = WorkManager.getInstance(context)

    fun schedule(predictionId: Long, reminderAtMs: Long, question: String) {
        val delay = reminderAtMs - System.currentTimeMillis()
        if (delay <= 0) return

        val data = workDataOf(
            ReminderWorker.KEY_PREDICTION_ID to predictionId,
            ReminderWorker.KEY_QUESTION to question,
        )
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(tagForPrediction(predictionId))
            .build()

        workManager.enqueueUniqueWork(
            "reminder_$predictionId",
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    fun cancel(predictionId: Long) {
        workManager.cancelUniqueWork("reminder_$predictionId")
    }

    fun cancelAll() {
        workManager.cancelAllWork()
    }

    private fun tagForPrediction(id: Long) = "reminder_$id"
}
```

- [ ] **Step 3: Wire NotificationScheduler into PredictionRepositoryImpl**

Modify `PredictionRepositoryImpl` to inject `NotificationScheduler` and schedule/cancel reminders on save:

```kotlin
// Modified PredictionRepositoryImpl.kt
package com.wisdometer.data.repository

import com.wisdometer.data.dao.PredictionDao
import com.wisdometer.data.model.Prediction
import com.wisdometer.data.model.PredictionOption
import com.wisdometer.data.model.PredictionWithOptions
import com.wisdometer.notifications.NotificationScheduler
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject

class PredictionRepositoryImpl @Inject constructor(
    private val dao: PredictionDao,
    private val notificationScheduler: NotificationScheduler,
) : PredictionRepository {

    override fun getAllPredictions(): Flow<List<PredictionWithOptions>> =
        dao.getAllWithOptions(Instant.now().toEpochMilli())

    override fun getPredictionById(id: Long): Flow<PredictionWithOptions?> =
        dao.getWithOptionsById(id)

    override suspend fun savePrediction(prediction: Prediction, options: List<PredictionOption>) {
        dao.upsertPredictionWithOptions(prediction, options)
        val reminder = prediction.reminderAt
        if (reminder != null && prediction.resolvedAt == null) {
            // Fetch the saved ID in case this was a new prediction
            notificationScheduler.schedule(
                predictionId = prediction.id.takeIf { it != 0L } ?: dao.getAllResolvedWithOptions()
                    .maxOfOrNull { it.prediction.id } ?: prediction.id,
                reminderAtMs = reminder.toEpochMilli(),
                question = prediction.question,
            )
        } else if (prediction.resolvedAt != null) {
            notificationScheduler.cancel(prediction.id)
        }
    }

    override suspend fun deletePrediction(prediction: Prediction) {
        notificationScheduler.cancel(prediction.id)
        dao.deletePrediction(prediction)
    }

    override suspend fun getAllResolved(): List<PredictionWithOptions> =
        dao.getAllResolvedWithOptions()

    override suspend fun importPredictions(items: List<PredictionWithOptions>) {
        for (item in items) {
            val existing = dao.countByQuestionAndCreatedAt(
                item.prediction.question,
                item.prediction.createdAt.toEpochMilli(),
            )
            if (existing == 0) {
                dao.upsertPredictionWithOptions(
                    item.prediction.copy(id = 0),
                    item.options.map { it.copy(id = 0, predictionId = 0) },
                )
            }
        }
    }
}
```

Note: The reminder scheduling after insert has a race condition with the auto-generated ID. Refactor `PredictionDao.upsertPredictionWithOptions` to return the inserted ID and use it:

```kotlin
// Updated DAO method to return the ID:
@Transaction
suspend fun upsertPredictionWithOptions(prediction: Prediction, options: List<PredictionOption>): Long {
    val id = if (prediction.id == 0L) {
        insertPrediction(prediction)
    } else {
        updatePrediction(prediction)
        prediction.id
    }
    deleteOptionsForPrediction(id)
    insertOptions(options.map { it.copy(predictionId = id) })
    return id
}
```

And update `PredictionRepositoryImpl.savePrediction` to use the returned ID:

```kotlin
override suspend fun savePrediction(prediction: Prediction, options: List<PredictionOption>) {
    val savedId = dao.upsertPredictionWithOptions(prediction, options)
    val reminder = prediction.reminderAt
    if (reminder != null && prediction.resolvedAt == null) {
        notificationScheduler.schedule(
            predictionId = savedId,
            reminderAtMs = reminder.toEpochMilli(),
            question = prediction.question,
        )
    } else if (prediction.resolvedAt != null) {
        notificationScheduler.cancel(savedId)
    }
}
```

- [ ] **Step 4: Verify compilation**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/main/kotlin/com/wisdometer/notifications/ app/src/main/kotlin/com/wisdometer/data/repository/PredictionRepositoryImpl.kt app/src/main/kotlin/com/wisdometer/data/dao/PredictionDao.kt
git commit -m "feat: WorkManager reminder notifications"
```

---

## Task 14: JSON Export / Import (TDD)

**Files:**
- Modify: `app/src/main/kotlin/com/wisdometer/export/JsonExporter.kt` (replace stub)
- Modify: `app/src/main/kotlin/com/wisdometer/export/JsonImporter.kt` (replace stub)
- Create: `app/src/test/kotlin/com/wisdometer/export/JsonExporterTest.kt`
- Create: `app/src/test/kotlin/com/wisdometer/export/JsonImporterTest.kt`

- [ ] **Step 1: Define the JSON data classes**

Add these serializable data classes inside a new file:

```kotlin
// app/src/main/kotlin/com/wisdometer/export/ExportModels.kt
package com.wisdometer.export

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExportFile(
    val version: Int = 1,
    @SerialName("exported_at") val exportedAt: String,
    val predictions: List<ExportedPrediction>,
)

@Serializable
data class ExportedPrediction(
    val id: Long,
    val question: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("reminder_at") val reminderAt: String? = null,
    @SerialName("resolved_at") val resolvedAt: String? = null,
    @SerialName("outcome_option_id") val outcomeOptionId: Long? = null,
    val tags: List<String>,
    val options: List<ExportedOption>,
)

@Serializable
data class ExportedOption(
    val label: String,
    val probability: Int,
    @SerialName("sort_order") val sortOrder: Int,
)
```

- [ ] **Step 2: Write the failing tests**

```kotlin
// app/src/test/kotlin/com/wisdometer/export/JsonExporterTest.kt
package com.wisdometer.export

import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class JsonExporterTest {

    private val converter = JsonConverter()

    @Test
    fun `toExportFile converts predictions with tags and options`() {
        val prediction = makePrediction(
            id = 1L,
            question = "Will I find a job?",
            tags = "career,finance",
            outcomeOptionId = 2L,
            resolvedAt = Instant.parse("2026-06-01T00:00:00Z"),
        )
        val options = listOf(
            makeOption(id = 1L, predictionId = 1L, label = "No", probability = 60, sortOrder = 0),
            makeOption(id = 2L, predictionId = 1L, label = "Yes", probability = 40, sortOrder = 1),
        )
        val withOptions = com.wisdometer.data.model.PredictionWithOptions(prediction, options)

        val exportFile = converter.toExportFile(listOf(withOptions))

        assertEquals(1, exportFile.version)
        assertEquals(1, exportFile.predictions.size)
        val ep = exportFile.predictions[0]
        assertEquals("Will I find a job?", ep.question)
        assertEquals(listOf("career", "finance"), ep.tags)
        assertEquals(2L, ep.outcomeOptionId)
        assertEquals("2026-06-01T00:00:00Z", ep.resolvedAt)
        assertEquals(2, ep.options.size)
        assertEquals("No", ep.options[0].label)
        assertEquals(60, ep.options[0].probability)
    }

    @Test
    fun `toExportFile with empty tags produces empty list`() {
        val prediction = makePrediction(id = 1L, question = "Q", tags = "")
        val opt = makeOption(id = 1L, predictionId = 1L, label = "Y", probability = 100, sortOrder = 0)
        val file = converter.toExportFile(listOf(com.wisdometer.data.model.PredictionWithOptions(prediction, listOf(opt))))
        assertEquals(emptyList<String>(), file.predictions[0].tags)
    }

    @Test
    fun `JSON round-trip preserves all fields`() {
        val prediction = makePrediction(id = 1L, question = "Round trip?", tags = "test")
        val opt = makeOption(id = 1L, predictionId = 1L, label = "Yes", probability = 100, sortOrder = 0)
        val original = converter.toExportFile(listOf(com.wisdometer.data.model.PredictionWithOptions(prediction, listOf(opt))))
        val json = Json.encodeToString(ExportFile.serializer(), original)
        val decoded = Json.decodeFromString(ExportFile.serializer(), json)
        assertEquals(original, decoded)
    }
}
```

```kotlin
// app/src/test/kotlin/com/wisdometer/export/JsonImporterTest.kt
package com.wisdometer.export

import org.junit.Assert.*
import org.junit.Test

class JsonImporterTest {

    private val converter = JsonConverter()

    @Test
    fun `fromExportFile converts options and tags correctly`() {
        val exportFile = ExportFile(
            version = 1,
            exportedAt = "2026-04-06T12:00:00Z",
            predictions = listOf(
                ExportedPrediction(
                    id = 42L,
                    question = "Test?",
                    createdAt = "2026-01-01T00:00:00Z",
                    reminderAt = null,
                    resolvedAt = null,
                    outcomeOptionId = null,
                    tags = listOf("a", "b"),
                    options = listOf(
                        ExportedOption("Yes", 70, 0),
                        ExportedOption("No", 30, 1),
                    ),
                )
            ),
        )
        val items = converter.fromExportFile(exportFile)
        assertEquals(1, items.size)
        val item = items[0]
        assertEquals("Test?", item.prediction.question)
        assertEquals("a,b", item.prediction.tags)
        assertNull(item.prediction.resolvedAt)
        assertEquals(2, item.options.size)
        assertEquals("Yes", item.options[0].label)
        assertEquals(70, item.options[0].probability)
    }

    @Test
    fun `fromExportFile preserves resolved_at and outcome_option_id`() {
        val exportFile = ExportFile(
            version = 1,
            exportedAt = "2026-04-06T12:00:00Z",
            predictions = listOf(
                ExportedPrediction(
                    id = 1L, question = "Q", createdAt = "2026-01-01T00:00:00Z",
                    resolvedAt = "2026-03-01T00:00:00Z", outcomeOptionId = 1L,
                    tags = emptyList(),
                    options = listOf(ExportedOption("Yes", 100, 0)),
                )
            ),
        )
        val items = converter.fromExportFile(exportFile)
        assertNotNull(items[0].prediction.resolvedAt)
        assertEquals(1L, items[0].prediction.outcomeOptionId)
    }
}

// Helpers shared between ExporterTest and ImporterTest
fun makePrediction(
    id: Long,
    question: String,
    tags: String = "",
    outcomeOptionId: Long? = null,
    resolvedAt: java.time.Instant? = null,
) = com.wisdometer.data.model.Prediction(
    id = id, question = question, tags = tags,
    outcomeOptionId = outcomeOptionId, resolvedAt = resolvedAt,
    createdAt = java.time.Instant.EPOCH,
)

fun makeOption(
    id: Long, predictionId: Long, label: String,
    probability: Int, sortOrder: Int,
) = com.wisdometer.data.model.PredictionOption(
    id = id, predictionId = predictionId, label = label,
    probability = probability, sortOrder = sortOrder,
)
```

- [ ] **Step 3: Run tests to verify they fail**

Run: `./gradlew test --tests "com.wisdometer.export.*"`
Expected: FAILED — `JsonConverter` class not found

- [ ] **Step 4: Create JsonConverter.kt (pure conversion logic, fully testable)**

```kotlin
// app/src/main/kotlin/com/wisdometer/export/JsonConverter.kt
package com.wisdometer.export

import com.wisdometer.data.model.Prediction
import com.wisdometer.data.model.PredictionOption
import com.wisdometer.data.model.PredictionWithOptions
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JsonConverter @Inject constructor() {

    fun toExportFile(items: List<PredictionWithOptions>): ExportFile = ExportFile(
        exportedAt = Instant.now().toString(),
        predictions = items.map { item ->
            ExportedPrediction(
                id = item.prediction.id,
                question = item.prediction.question,
                createdAt = item.prediction.createdAt.toString(),
                reminderAt = item.prediction.reminderAt?.toString(),
                resolvedAt = item.prediction.resolvedAt?.toString(),
                outcomeOptionId = item.prediction.outcomeOptionId,
                tags = if (item.prediction.tags.isBlank()) emptyList()
                       else item.prediction.tags.split(",").map { it.trim() }.filter { it.isNotBlank() },
                options = item.sortedOptions.map { opt ->
                    ExportedOption(opt.label, opt.probability, opt.sortOrder)
                },
            )
        },
    )

    fun fromExportFile(file: ExportFile): List<PredictionWithOptions> =
        file.predictions.map { ep ->
            val prediction = Prediction(
                id = 0,  // will be re-assigned on import
                question = ep.question,
                createdAt = Instant.parse(ep.createdAt),
                reminderAt = ep.reminderAt?.let { Instant.parse(it) },
                resolvedAt = ep.resolvedAt?.let { Instant.parse(it) },
                outcomeOptionId = ep.outcomeOptionId,
                tags = ep.tags.joinToString(","),
            )
            val options = ep.options.mapIndexed { i, opt ->
                PredictionOption(
                    id = 0,
                    predictionId = 0,
                    label = opt.label,
                    probability = opt.probability,
                    sortOrder = opt.sortOrder.takeIf { it >= 0 } ?: i,
                )
            }
            PredictionWithOptions(prediction, options)
        }
}
```

- [ ] **Step 5: Replace JsonExporter stub with full implementation**

```kotlin
// app/src/main/kotlin/com/wisdometer/export/JsonExporter.kt
package com.wisdometer.export

import android.content.Context
import android.net.Uri
import com.wisdometer.data.repository.PredictionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JsonExporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: PredictionRepository,
    private val converter: JsonConverter,
) {
    private val json = Json { prettyPrint = true }

    suspend fun exportToUri(uri: Uri) {
        val all = repository.getAllResolved() + getAllUnresolved()
        val exportFile = converter.toExportFile(all)
        val jsonString = json.encodeToString(ExportFile.serializer(), exportFile)
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(jsonString.toByteArray(Charsets.UTF_8))
        } ?: error("Cannot open output stream for URI: $uri")
    }

    // getAllResolved only gives resolved; we need all predictions for export
    private suspend fun getAllUnresolved(): List<com.wisdometer.data.model.PredictionWithOptions> = emptyList()
    // Note: Replace PredictionRepository.getAllResolved with PredictionRepository.getAll for export.
    // Simplest fix: add getAllPredictions as a suspend fun that collects the flow once.
}
```

Note: The exporter needs all predictions, not just resolved. Add a `suspend fun getAll()` to the repository interface and implementation:

```kotlin
// In PredictionRepository interface, add:
suspend fun getAll(): List<PredictionWithOptions>

// In PredictionRepositoryImpl, add:
override suspend fun getAll(): List<PredictionWithOptions> =
    getAllPredictions().first()
```

Then update `JsonExporter.exportToUri`:
```kotlin
suspend fun exportToUri(uri: Uri) {
    val all = repository.getAll()
    val exportFile = converter.toExportFile(all)
    val jsonString = json.encodeToString(ExportFile.serializer(), exportFile)
    context.contentResolver.openOutputStream(uri)?.use { stream ->
        stream.write(jsonString.toByteArray(Charsets.UTF_8))
    } ?: error("Cannot open output stream for URI: $uri")
}
```

- [ ] **Step 6: Replace JsonImporter stub with full implementation**

```kotlin
// app/src/main/kotlin/com/wisdometer/export/JsonImporter.kt
package com.wisdometer.export

import android.content.Context
import android.net.Uri
import com.wisdometer.data.repository.PredictionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JsonImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: PredictionRepository,
    private val converter: JsonConverter,
) {
    private val json = Json { ignoreUnknownKeys = true }

    /** Returns count of newly imported predictions. */
    suspend fun importFromUri(uri: Uri): Int {
        val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.readBytes().toString(Charsets.UTF_8)
        } ?: error("Cannot open input stream for URI: $uri")

        val exportFile = json.decodeFromString(ExportFile.serializer(), jsonString)
        val items = converter.fromExportFile(exportFile)
        repository.importPredictions(items)
        return items.size
    }
}
```

- [ ] **Step 7: Run tests to verify they pass**

Run: `./gradlew test --tests "com.wisdometer.export.*"`
Expected: BUILD SUCCESSFUL, all tests pass

- [ ] **Step 8: Compile the full app**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 9: Commit**

```bash
git add app/src/main/kotlin/com/wisdometer/export/ app/src/test/kotlin/com/wisdometer/export/ app/src/main/kotlin/com/wisdometer/data/repository/
git commit -m "feat: JSON export/import with tests"
```

---

## Task 15: Share as Image

**Files:**
- Create: `app/src/main/kotlin/com/wisdometer/share/ShareImageRenderer.kt`
- Modify: `app/src/main/kotlin/com/wisdometer/ui/detail/PredictionDetailScreen.kt`
- Modify: `app/src/main/kotlin/com/wisdometer/ui/profile/ProfileScreen.kt`

- [ ] **Step 1: Create ShareImageRenderer.kt**

```kotlin
// app/src/main/kotlin/com/wisdometer/share/ShareImageRenderer.kt
package com.wisdometer.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ShareImageRenderer {

    /**
     * Renders a simple prediction summary to a Bitmap and shares it via ACTION_SEND.
     * For full Compose-to-bitmap rendering, use a Compose AndroidView approach or
     * the ComposeView capture technique if you need richer layouts.
     *
     * This implementation draws a clean summary card using Canvas directly.
     */
    fun sharePredictionCard(
        context: Context,
        question: String,
        options: List<Pair<String, Int>>,  // label to probability
        isResolved: Boolean,
        actualOptionLabel: String?,
    ) {
        val bitmap = renderPredictionCard(question, options, isResolved, actualOptionLabel)
        shareImageBitmap(context, bitmap, "prediction")
    }

    fun shareProfileStats(
        context: Context,
        accuracy: Int,
        brierScore: Double,
        totalPredictions: Int,
        resolvedPredictions: Int,
    ) {
        val bitmap = renderProfileSummary(accuracy, brierScore, totalPredictions, resolvedPredictions)
        shareImageBitmap(context, bitmap, "profile")
    }

    private fun renderPredictionCard(
        question: String,
        options: List<Pair<String, Int>>,
        isResolved: Boolean,
        actualOptionLabel: String?,
    ): Bitmap {
        val width = 900
        val rowHeight = 80
        val height = 120 + options.size * rowHeight + 80
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint().apply { color = 0xFFFAFAF8.toInt(); isAntiAlias = true }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val textPaint = Paint().apply { color = 0xFF1A1A1A.toInt(); textSize = 40f; isAntiAlias = true; isFakeBoldText = true }
        canvas.drawText(question.take(60), 40f, 70f, textPaint)

        val statusText = if (isResolved) "RESOLVED" else "OPEN"
        val statusBgColor = if (isResolved) 0xFFD4EDDA.toInt() else 0xFFFFF3CD.toInt()
        val statusTextColor = if (isResolved) 0xFF155724.toInt() else 0xFF856404.toInt()
        val statusPaint = Paint().apply { color = statusBgColor; isAntiAlias = true }
        canvas.drawRoundRect(RectF(700f, 30f, 860f, 80f), 8f, 8f, statusPaint)
        val statusTextPaint = Paint().apply { color = statusTextColor; textSize = 28f; isAntiAlias = true }
        canvas.drawText(statusText, 720f, 65f, statusTextPaint)

        val barColors = listOf(0xFF4A90D9.toInt(), 0xFF7EC8A4.toInt(), 0xFFE8A44A.toInt(), 0xFFD96A6A.toInt())
        options.forEachIndexed { i, (label, probability) ->
            val y = 120f + i * rowHeight
            val labelPaint = Paint().apply {
                color = if (label == actualOptionLabel) barColors[i % barColors.size] else 0xFF6B6B6B.toInt()
                textSize = 32f; isAntiAlias = true
            }
            canvas.drawText("${if (label == actualOptionLabel) "✓ " else ""}$label: $probability%", 40f, y + 30f, labelPaint)
            val barBg = Paint().apply { color = barColors[i % barColors.size]; alpha = 40; isAntiAlias = true }
            val barFg = Paint().apply { color = barColors[i % barColors.size]; isAntiAlias = true }
            val barTop = y + 40f
            val barBottom = y + 56f
            canvas.drawRoundRect(RectF(40f, barTop, 860f, barBottom), 6f, 6f, barBg)
            canvas.drawRoundRect(RectF(40f, barTop, 40f + 820f * probability / 100f, barBottom), 6f, 6f, barFg)
        }

        val footerPaint = Paint().apply { color = 0xFF6B6B6B.toInt(); textSize = 24f; isAntiAlias = true }
        canvas.drawText("Wisdometer", 40f, height - 20f, footerPaint)
        return bitmap
    }

    private fun renderProfileSummary(
        accuracy: Int,
        brierScore: Double,
        totalPredictions: Int,
        resolvedPredictions: Int,
    ): Bitmap {
        val width = 900
        val height = 400
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint().apply { color = 0xFFFAFAF8.toInt() }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val titlePaint = Paint().apply { color = 0xFF1A1A1A.toInt(); textSize = 48f; isAntiAlias = true; isFakeBoldText = true }
        canvas.drawText("My Prediction Accuracy", 40f, 80f, titlePaint)

        val bigPaint = Paint().apply { color = 0xFF4A90D9.toInt(); textSize = 100f; isAntiAlias = true; isFakeBoldText = true }
        canvas.drawText("$accuracy%", 40f, 220f, bigPaint)

        val subPaint = Paint().apply { color = 0xFF6B6B6B.toInt(); textSize = 36f; isAntiAlias = true }
        canvas.drawText("Brier Score: ${"%.2f".format(brierScore)}", 40f, 280f, subPaint)
        canvas.drawText("Total: $totalPredictions  •  Resolved: $resolvedPredictions", 40f, 330f, subPaint)

        val footerPaint = Paint().apply { color = 0xFF6B6B6B.toInt(); textSize = 28f; isAntiAlias = true }
        canvas.drawText("Wisdometer", 40f, 380f, footerPaint)
        return bitmap
    }

    private fun shareImageBitmap(context: Context, bitmap: Bitmap, filePrefix: String) {
        val imagesDir = File(context.cacheDir, "images").also { it.mkdirs() }
        val imageFile = File(imagesDir, "${filePrefix}_share.png")
        FileOutputStream(imageFile).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share prediction"))
    }
}
```

- [ ] **Step 2: Add Share button to PredictionDetailScreen**

Add a share icon button to the top bar in `PredictionDetailScreen.kt`. Add this import and button alongside the edit button:

```kotlin
import androidx.compose.material.icons.filled.Share
import com.wisdometer.share.ShareImageRenderer

// In the top bar Row, after the edit icon button:
val ctx = androidx.compose.ui.platform.LocalContext.current
IconButton(onClick = {
    pw?.let { item ->
        ShareImageRenderer.sharePredictionCard(
            context = ctx,
            question = item.prediction.question,
            options = item.sortedOptions.map { it.label to it.probability },
            isResolved = item.isResolved,
            actualOptionLabel = item.actualOption?.label,
        )
    }
}) {
    Icon(Icons.Default.Share, contentDescription = "Share")
}
```

This should be placed inside the `item?.let { pw ->` block so `pw` is accessible.

- [ ] **Step 3: Add Share button to ProfileScreen**

Add at the end of the Profile screen column, after the tag breakdown card:

```kotlin
import com.wisdometer.share.ShareImageRenderer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share

val ctx = LocalContext.current
OutlinedButton(
    onClick = {
        ShareImageRenderer.shareProfileStats(
            context = ctx,
            accuracy = (state.simpleCloseness * 100).roundToInt(),
            brierScore = state.brierScore,
            totalPredictions = state.totalPredictions,
            resolvedPredictions = state.resolvedPredictions,
        )
    },
    modifier = Modifier.fillMaxWidth(),
) {
    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
    Text("Share Stats")
}
```

- [ ] **Step 4: Verify compilation**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Run all tests**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL, all tests pass

- [ ] **Step 6: Commit**

```bash
git add app/src/main/kotlin/com/wisdometer/share/ app/src/main/kotlin/com/wisdometer/ui/detail/PredictionDetailScreen.kt app/src/main/kotlin/com/wisdometer/ui/profile/ProfileScreen.kt
git commit -m "feat: share as image for prediction and profile screens"
```

---

## Final Build Verification

- [ ] **Run full build**

```bash
./gradlew assembleDebug
```
Expected: BUILD SUCCESSFUL

- [ ] **Run all tests**

```bash
./gradlew test
```
Expected: All tests pass

- [ ] **Final commit**

```bash
git add -A
git commit -m "feat: complete Wisdometer v1 implementation"
```

---

## Self-Review Against Spec

**Spec coverage check:**

| Requirement | Task |
|---|---|
| Prediction + PredictionOption data model | Task 2 |
| Room database | Tasks 3–4 |
| Predictions list screen with sort order | Task 8 |
| Filter bar: All/Open/Resolved + tag chips | Task 8 |
| Compact mode card | Tasks 6, 12 |
| FAB → New prediction | Tasks 7, 8 |
| New/Edit prediction screen (full-screen bottom sheet) | Task 9 |
| Options with label + probability, sum validation | Task 9 |
| Reminder date picker | Task 9 (field present; date picker UI is a text field — add DatePickerDialog in Step 2 if desired) |
| Tag chip input | Task 9 |
| Prediction Detail: options + probabilities | Task 10 |
| Set outcome button | Task 10 |
| Edit button | Task 10 |
| Share as image (Detail) | Task 15 |
| Profile: accuracy score | Task 11 |
| Brier score + tooltip | Task 11 |
| Accuracy by tag | Task 11 |
| Accuracy graph (toggle time/count) | Task 11 |
| Summary stats | Task 11 |
| Share as image (Profile) | Task 15 |
| Settings: compact toggle | Task 12 |
| Settings: notifications toggle | Task 12 |
| Settings: Export JSON | Tasks 12, 14 |
| Settings: Import JSON | Tasks 12, 14 |
| Simple closeness scoring | Task 5 |
| Brier score | Task 5 |
| WorkManager notifications | Task 13 |
| JSON format v1 | Task 14 |
| Import merge by question+created_at | Tasks 4, 14 |
| Visual style (#FAFAF8, badges, bars) | Task 6 |
| Bottom nav: Predictions/Profile/Settings | Task 7 |
| Min SDK 26 | Task 1 |

**Gap identified:** The spec mentions a date picker for `reminderAt`. Task 9 creates a text field for tags but doesn't implement a proper `DatePickerDialog`. Add this to `EditPredictionScreen` after the tags field:

```kotlin
// After the tags OutlinedTextField in EditPredictionScreen:
var showDatePicker by remember { mutableStateOf(false) }
OutlinedButton(
    onClick = { showDatePicker = true },
    modifier = Modifier.fillMaxWidth(),
) {
    val reminderText = state.reminderAt?.let {
        java.time.ZonedDateTime.ofInstant(it, java.time.ZoneId.systemDefault())
            .format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy"))
    } ?: "Set reminder (optional)"
    Text(reminderText)
}
if (showDatePicker) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.reminderAt?.toEpochMilli()
    )
    DatePickerDialog(
        onDismissRequest = { showDatePicker = false },
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    viewModel.setReminder(java.time.Instant.ofEpochMilli(it))
                }
                showDatePicker = false
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = { showDatePicker = false }) { Text("Clear") }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}
```

Add the import `androidx.compose.material3.rememberDatePickerState` and `DatePickerDialog` and `DatePicker`.
