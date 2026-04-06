# Wisdometer

Offline Android app for tracking personal predictions. Works like a prediction market — without money, bets, or crypto. Just questions, estimated probabilities per outcome, and accuracy tracking over time.

## Features

- Create predictions with multiple options and probability estimates (must sum to 100%)
- Resolve predictions by marking which outcome occurred
- Accuracy tracking: simple closeness score and Brier score
- Profile screen with calibration chart, confidence distribution, and accuracy over time
- Tag predictions and see accuracy broken down by tag
- Optional reminder notifications via WorkManager
- Export / import as JSON
- Share predictions and stats as images
- Compact / normal list density toggle

## Tech stack

- **Language:** Kotlin
- **UI:** Jetpack Compose with custom dark theme
- **Architecture:** MVVM — ViewModel + StateFlow + Repository
- **Database:** Room (SQLite)
- **DI:** Hilt
- **Background work:** WorkManager
- **Min SDK:** 26 (Android 8.0)

## Requirements

- JDK 17 (`/usr/lib/jvm/java-17-openjdk`)
- Android SDK with platform-tools (`~/Android/Sdk`)
- A connected device or emulator for `make install`

## Build

```sh
make build          # debug APK
make build-release  # release APK
make bundle         # release AAB
make install        # build + install on connected device
make test           # unit tests
make lint           # lint
make clean          # clean build outputs
```

See `make tasks` for the full Gradle task list and `make env` to verify your environment.

## Project structure

```
app/src/main/kotlin/com/wisdometer/
├── data/
│   ├── dao/          Room DAO
│   ├── db/           Database + type converters
│   ├── model/        Prediction, PredictionOption, PredictionWithOptions
│   └── repository/   PredictionRepository (interface + impl)
├── di/               Hilt modules
├── domain/           ScoringEngine (accuracy, Brier, calibration)
├── export/           JSON export / import
├── notifications/    ReminderWorker, NotificationScheduler
├── share/            Share-as-image renderer
└── ui/
    ├── components/   PredictionCard, ProbabilityBar, StatusBadge
    ├── detail/       Prediction detail screen
    ├── edit/         New / edit prediction screen
    ├── navigation/   NavGraph
    ├── predictions/  Prediction list screen
    ├── profile/      Profile screen + charts
    ├── settings/     Settings screen
    └── theme/        Colors, typography, theme
```

## Accuracy scoring

**Simple closeness** — `probability_of_actual_outcome / 100`, averaged across resolved predictions. Shown as a percentage (e.g. "73% accuracy").

**Brier score** — `(p_actual − 1)² + Σ(p_other)²`, averaged across resolved predictions. 0.0 = perfect, 2.0 = worst.

**Calibration** — for each 10%-wide probability bucket, what fraction of options in that bucket actually occurred? A well-calibrated forecaster tracks the diagonal.
