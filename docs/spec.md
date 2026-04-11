# Product Specification

## Overview

A personal forecasting app that helps users track prediction accuracy over time.
Users create predictions with multiple options and probability estimates, then record outcomes to measure calibration and scoring metrics.
All data is local; JSON files are the import/export format.

## Data Model

### Prediction

Represents a forecasting question with metadata and resolution state.

| Field | Type | Column | Notes |
| --- | --- | --- | --- |
| `id` | `Long` | `id` | Auto-generated primary key |
| `title` | `String` | `question` | The prediction question text |
| `description` | `String` | `description` | Optional context (default: "") |
| `createdAt` | `Instant` | `created_at` | Creation timestamp |
| `updatedAt` | `Instant?` | `updated_at` | Last modification timestamp |
| `reminderAt` | `Instant?` | `reminder_at` | End date / reminder trigger |
| `resolvedAt` | `Instant?` | `resolved_at` | When outcome was set |
| `outcomeOptionId` | `Long?` | `outcome_option_id` | FK to selected outcome option |
| `tags` | `String` | `tags` | Comma-separated tags (default: "") |

### PredictionOption

Represents one possible outcome for a prediction.

| Field | Type | Column | Notes |
| --- | --- | --- | --- |
| `id` | `Long` | `id` | Auto-generated primary key |
| `predictionId` | `Long` | `prediction_id` | FK → Prediction.id (CASCADE delete) |
| `label` | `String` | `label` | Option text (e.g., "Yes", "No") |
| `probability` | `Int` | `probability` | 0–100; all options must sum to 100 |
| `sortOrder` | `Int` | `sort_order` | Display order index |

### PredictionWithOptions (Relation)

One Prediction with its list of PredictionOptions. Computed properties:

- `isResolved` — true if `resolvedAt != null`
- `sortedOptions` — options sorted by sortOrder
- `actualOption` — the option matching `outcomeOptionId`

## Architecture

Single-module Android app (Kotlin, Jetpack Compose, Room, Hilt, Material 3)
with three layers:

```text
ui/         — Compose screens + ViewModels (StateFlow)
domain/     — ScoringEngine (pure Kotlin, no Android deps)
data/       — Room database, DAO, JSON serialization, repository
```

**State management:** ViewModel per screen, `StateFlow` exposed to Compose.
**Dependency injection:** Hilt (`@HiltViewModel`, `@Singleton`, `@InstallIn`).
**Persistence:** Room SQLite (v1, `fallbackToDestructiveMigration()`).
**Serialization:** `kotlinx.serialization` for JSON.
**Notifications:** WorkManager with HiltWorkerFactory.

Three tabs: **Predictions** | **Profile** | **Settings**
Overlay screens: **Detail** | **Edit/Create**

## Computation Logic

### Simple Closeness

```text
simpleCloseness = probability_of_actual_outcome / 100
averaged across all resolved predictions
range: [0.0, 1.0]
```

### Brier Score

```text
brierScore = (p_actual - 1)² + Σ(p_other²)
averaged across all resolved predictions
range: [0.0, 2.0]   (0 = perfect, 2 = worst)
```

### Calibration

```text
For each 10%-wide probability bucket (midpoints: 5, 15, ..., 95):
  actualRate = count(actual outcomes in bucket) / total_options_in_bucket
  → CalibrationPoint(predictedPct, actualRate, sampleCount)
```

### Confidence Distribution

```text
For each 10%-wide bucket:
  count predictions whose top-ranked option probability falls in that range
  → List<Pair<bucketMidpoint, count>>
```

### Accuracy Over Time / Count

Cumulative rolling average of simple closeness, sorted by resolution timestamp or prediction index.

### Tag-based Scoring

Filters resolved predictions by tag, then computes simple closeness and Brier score independently per tag.

### Average Confidence

Mean of each prediction's top-ranked option probability across all predictions.

## Import / Export

### JSON Schema

```json
{
  "version": 1,
  "exported_at": "ISO-8601 timestamp",
  "predictions": [
    {
      "id": 1,
      "question": "Will X happen?",
      "description": "",
      "created_at": "2025-01-15T10:00:00Z",
      "updated_at": null,
      "reminder_at": "2025-06-01T00:00:00Z",
      "resolved_at": null,
      "outcome_option_index": null,
      "tags": ["career", "finance"],
      "options": [
        { "label": "Yes", "probability": 70, "sort_order": 0 },
        { "label": "No", "probability": 30, "sort_order": 1 }
      ]
    }
  ]
}
```

### Behavior

- **Export:** writes all predictions to user-chosen file via SAF; filename `wisdometer-export-YYYY-MM-DD.json`
- **Import:** reads file, deduplicates by (question + createdAt), inserts new predictions with regenerated IDs
- **Outcome remapping:** `outcome_option_index` maps to new option IDs after insert
- **Forward compatibility:** `ignoreUnknownKeys = true`
- **Field mapping:** `Prediction.title` ↔ `ExportedPrediction.question`; `Prediction.tags` (comma-string) ↔ `List<String>`

## Notifications

- WorkManager `OneTimeWorkRequest` scheduled at `reminderAt` time
- Tag: `reminder_{predictionId}`, policy: REPLACE
- Skips if prediction already resolved
- Notification: title "Time to resolve", text = prediction question
- Tapping opens Detail screen via intent extra `prediction_id`
- Cancelled on resolution or deletion

## Settings

| Key | Type | Default | Description |
| --- | --- | --- | --- |
| `welcome_seen` | Boolean | false | First-run gate |
| `compact_mode` | Boolean | false | Condensed prediction cards |
| `notifications_enabled` | Boolean | true | Reminder notifications on/off |

All stored in SharedPreferences file `wisdometer_settings`.

## Build & CI

| Tool | Version | Purpose |
| --- | --- | --- |
| AGP | 8.9.2 | Android Gradle Plugin |
| Kotlin | 2.1.20 | Language |
| KSP | 2.1.20-1.0.31 | Annotation processing |
| Compose BOM | 2024.12.01 | UI toolkit |
| Room | 2.6.1 | Database |
| Hilt | 2.56.1 | Dependency injection |
| WorkManager | 2.10.0 | Background scheduling |
| JDK | 21 | Build toolchain |

**Targets:** Compile SDK 35, Min SDK 26 (Android 8.0), Target SDK 35.

`make all` runs lint + test.

## Out of Scope (v1)

- Cloud sync
- Multi-user / social features
- Prediction markets integration
- Light theme
- Widgets
