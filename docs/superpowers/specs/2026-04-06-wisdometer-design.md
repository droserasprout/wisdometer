# Wisdometer — Design Spec

**Date:** 2026-04-06  
**Status:** Approved

## Overview

Wisdometer is an offline Android app for tracking personal predictions about arbitrary future events. It works like a prediction market but without money, bets, or crypto — just questions, estimated probabilities per outcome, and accuracy tracking over time.

---

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose (custom theme, no Material You)
- **Architecture:** MVVM — ViewModel + StateFlow, Repository pattern
- **Database:** Room (SQLite)
- **DI:** Hilt
- **Background work:** WorkManager (reminder notifications)
- **Min SDK:** API 26 (Android 8.0)

---

## Data Model

### Prediction
| Field | Type | Notes |
|---|---|---|
| id | Long | Primary key |
| question | String | The prediction statement |
| createdAt | Instant | |
| reminderAt | Instant? | Optional reminder date |
| resolvedAt | Instant? | Null = unresolved |
| outcomeOptionId | Long? | FK → PredictionOption that occurred; null = unresolved |
| tags | String | Comma-separated, stored as single column |

### PredictionOption
| Field | Type | Notes |
|---|---|---|
| id | Long | Primary key |
| predictionId | Long | Foreign key → Prediction |
| label | String | e.g. "No with interviews" |
| probability | Int | 0–100; all options for a prediction must sum to 100 |
| sortOrder | Int | Display order |

Accuracy is computed on-the-fly from resolved predictions — not stored.

---

## Screens & Navigation

Bottom navigation bar: **Predictions** | **Profile** | **Settings**

### Predictions Screen
- List of all predictions sorted by: upcoming reminder first → unresolved → resolved
- Filter bar: All / Open / Resolved + tag filter chips
- Each card: question, probability bars per option, status badge (Open / Resolved), reminder date, tags
- **Compact mode:** same card, reduced padding, smaller text, thinner bars
- FAB: "New prediction"
- Tap card → Prediction Detail screen

### New / Edit Prediction Screen (full-screen bottom sheet)
- Question text field
- Options list: label + probability % per row; add/remove rows; live validation that all probabilities sum to 100%
- Optional: reminder date picker
- Optional: tag chip input (free text)

### Prediction Detail Screen
- Full view of prediction with all options and probabilities
- "Set outcome" button (if unresolved) — selects which option occurred
- Edit button
- Share as image button

### Profile Screen
- Overall accuracy score (simple closeness %) prominently displayed
- Brier score secondary, with expandable tooltip explaining it
- Accuracy breakdown by tag
- Graph: accuracy over time OR by prediction count (toggle between X-axes)
- Summary stats: total / resolved / open / avg confidence (mean probability assigned to the user's top-ranked option across all predictions)

### Settings Screen
- Compact / Normal mode toggle
- Notifications on/off
- Export JSON button
- Import JSON button

---

## Accuracy Scoring

Computed on-the-fly from resolved predictions only.

### Simple Closeness
For each resolved prediction, score = `probability_of_actual_outcome / 100`.  
- 100% on correct outcome → score 1.0
- 10% on correct outcome → score 0.1

Averaged across all resolved predictions, displayed as a percentage. E.g. "73% accuracy".

Computed globally and per-tag.

### Brier Score
`score = (p - 1)² + Σ(p_other)²` where `p` is the probability (as decimal 0–1) of the actual outcome.  
- 0.0 = perfect, 2.0 = worst possible
- Displayed with tooltip: *"Brier score measures calibration — 0.0 is perfect, 2.0 is worst."*

Computed globally and per-tag.

---

## Compact / Normal Mode

Controls list density only. Same information shown in both modes.

- **Normal:** standard card padding, regular text sizes, standard bar height
- **Compact:** reduced vertical padding, smaller font sizes, thinner probability bars — more predictions visible per screen

Toggled in Settings, persisted in SharedPreferences.

---

## JSON Import / Export

### Export format
```json
{
  "version": 1,
  "exported_at": "2026-04-06T12:00:00Z",
  "predictions": [
    {
      "id": 1,
      "question": "Will I find a job by 2027?",
      "created_at": "2026-01-01T00:00:00Z",
      "reminder_at": "2027-01-01T00:00:00Z",
      "resolved_at": null,
      "outcome_option_id": null,
      "tags": ["career"],
      "options": [
        {"label": "No with interviews", "probability": 50, "sort_order": 0},
        {"label": "No without interviews", "probability": 40, "sort_order": 1},
        {"label": "Yes", "probability": 10, "sort_order": 2}
      ]
    }
  ]
}
```

### Import behavior
- Reads the same format
- Merges by `question` + `created_at` — skips exact duplicates, imports new entries
- Non-destructive: never overwrites or deletes existing predictions

---

## Notifications

Powered by WorkManager. When a prediction has `reminderAt` set:
- Schedule a one-time notification on that date: *"Time to resolve: '[question]'"*
- Tapping opens the Prediction Detail screen for that prediction
- If the prediction is already resolved when the date arrives, no notification fires
- Notifications master toggle in Settings disables/cancels all scheduled notifications

---

## Share as Image

Available on:
- Prediction Detail screen (shares the prediction card with options + probabilities)
- Profile screen (shares accuracy stats summary)

Implementation: render a Compose layout off-screen to a `Bitmap` via `Canvas`, share via Android share sheet (`ACTION_SEND`, `image/png`).

---

## Visual Style

- Light background (`#FAFAF8`)
- Card-based layout with subtle shadow
- Inline probability bars per option (colored, proportional width)
- Status badges: OPEN (amber), RESOLVED (green)
- Custom theme — no Material You / dynamic color
- Dark mode: out of scope for v1
