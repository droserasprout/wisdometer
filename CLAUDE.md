# Wisdometer - Claude Code Guidelines

## Build & Test

```sh
make build      # debug APK (assembleDebug)
make test       # unit tests
make compile    # compile only (fast check)
make lint       # lint
make install    # build + install on device
make test-one CLASS=com.wisdometer.domain.ScoringEngineTest  # single test class
```

Always use the Makefile — it sets JAVA_HOME and ANDROID_HOME correctly.

## Architecture

- **MVVM**: Screen → ViewModel (StateFlow) → Repository → Room DAO
- **DI**: Hilt — modules in `di/`, `@HiltViewModel` on ViewModels
- **Always-dark theme**: No light theme — `darkColorScheme()` only, no `isSystemInDarkTheme()` checks
- **Custom colors**: `LocalWisdometerColors` (via `CompositionLocalProvider`) for colors not in Material3 palette (badges, chart grid, resolved alpha)

## Key Conventions

- **Room column naming**: Use `@ColumnInfo(name = "snake_case")` when Kotlin field name differs from desired column name. Migrations must match.
- **DB version**: Currently 2. Migrations live in `data/db/WisdometerDatabase.kt`.
- **Prediction.title**: Field is `title` in Kotlin, column is `question` in DB (historical rename with `@ColumnInfo`).
- **Tags**: Stored as comma-separated string in DB, converted via `Prediction.tagList` extension property.
- **Card colors**: Always pass explicit `containerColor = MaterialTheme.colorScheme.surface` to `Card` — Material3 default differs from our theme's `surface`.
- **Button shapes**: Material3 `Button` ignores `MaterialTheme.shapes` — pass `shape = RoundedCornerShape(8.dp)` directly.
- **Metadata labels**: Use `MaterialTheme.typography.labelSmall` + `MaterialTheme.colorScheme.onSurfaceVariant` for all secondary metadata (tags, dates, stats).
- **Canvas text**: Use `rememberTextMeasurer()` + `drawText()` for chart axis labels (not `nativeCanvas`).

## Testing

Unit tests in `app/src/test/`. Key test files:
- `ScoringEngineTest` — accuracy and scoring logic
- `JsonExporterTest` / `JsonImporterTest` — export/import round-trip
- `Prediction` model uses `title` field; `ExportedPrediction` uses `question` field (different names, don't mix up in tests)
