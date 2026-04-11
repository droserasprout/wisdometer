# Design System

## Colors

### Theme (Dark only)

| Token | Hex | Usage |
|-------|-----|-------|
| Background | #222222 | Screen background |
| Surface | #2E2B28 | Cards, elevated surfaces |
| OnBackground / OnSurface | #EAEAE8 | Primary text |
| OnSurfaceVariant | #9A9A9A | Secondary text, metadata |
| SurfaceVariant | #3A3632 | Variant surfaces |

### Badge Colors (Dark)

| Name | Hex | Usage |
|------|-----|-------|
| BadgeOpenBackground | #3D2E00 | Unresolved prediction badge background |
| BadgeOpenText | #FFFD66 | Unresolved prediction badge text |
| BadgeResolvedBackground | #0D3318 | Resolved prediction badge background |
| BadgeResolvedText | #6BCF8B | Resolved prediction badge text |

### Chart & Data Visualization

| Name | Hex | Usage |
|------|-----|-------|
| ChartGridLine | #3A3A3A | Grid lines in charts |
| BarColor1 | #4A90D9 | Blue - probability bars, charts |
| BarColor2 | #7EC8A4 | Teal/Green - probability bars, charts |
| BarColor3 | #E8A44A | Orange - probability bars, charts |
| BarColor4 | #D96A6A | Red/Coral - probability bars, charts |
| BarColor5 | #9B7EC8 | Purple - probability bars, charts |
| BarColor6 | #4AC8C8 | Cyan - probability bars, charts |

### Resolved Card Alpha

| Theme | Value | Usage |
|-------|-------|-------|
| Dark | 0.4f | Opacity for resolved prediction cards |

### Custom Colors Access

Material3 colors via `MaterialTheme.colorScheme.*`. App-specific colors via `LocalWisdometerColors.current.*` (provided through `CompositionLocalProvider`).

## Typography

Custom `WisdometerTypography` overriding Material3 defaults:

| Style | Size | Weight | Used for |
|-------|------|--------|----------|
| headlineLarge | 24sp | Bold | Screen titles (Predictions, Profile, Settings) |
| headlineMedium | 20sp | SemiBold | Card titles, section headers |
| titleMedium | 16sp | Medium | Section subtitles (Options, Calibration) |
| bodyMedium | 14sp | Normal | Body text, descriptions |
| bodySmall | 12sp | Normal | Secondary descriptions |
| labelSmall | 11sp | Medium | Metadata labels, tags, dates |

### Special Sizes (inline)

| Size | Weight | Usage |
|------|--------|-------|
| 32sp | Bold | Welcome screen title |
| 28sp | Bold | Stat tile values |
| 10sp | Medium | Badge text, chart axis labels |

## Shapes

| Radius | Usage |
|--------|-------|
| 4dp | Badge labels, chart bar corners |
| 8dp | Buttons (OutlinedButton, primary Button) |
| 12dp | Cards (PredictionCard, stat tiles, chart containers) |
| 50% (pill) | Probability bar fill |

## Shared Components

### PredictionCard

- Two modes: compact and expanded
- Card shape: RoundedCornerShape(12dp)
- Elevation: 2dp
- Compact padding: 10dp; regular padding: 16dp
- Option spacing: 2dp (compact), 4dp (regular)
- Displays: title, description, options with ProbabilityBar, StatusBadge, tags, dates
- containerColor: `MaterialTheme.colorScheme.surface`

### StatusBadge

- Displays "OPEN" or "RESOLVED" (uppercase)
- Font: 10sp, Medium weight
- Padding: 6dp horizontal, 2dp vertical
- Corner radius: 4dp
- Colors from LocalWisdometerColors (badge background/text pairs)

### ProbabilityBar

- Label + percentage text + colored bar
- Bar colors cycle through BarColors palette (6 colors)
- Compact bar height: 6dp; regular: 10dp
- Vertical padding: 2dp (compact), 4dp (regular)
- Corner radius: 50% (fully rounded pill)
- Emoji indicators: `🎯` actual outcome, `🔮` top prediction, `🎯🔮` both

### StatTile

- Card with colored accent bar on top
- Card corner radius: 12dp
- Accent bar height: 3dp
- Inner padding: 12dp
- Value: 28sp Bold
- Label: labelSmall

### AccuracyDonut

- Circular progress indicator
- Default size: 72dp
- Stroke width: 15% of diameter
- Rounded stroke cap

### Charts (Canvas-based)

#### AccuracyChart
- Padding: L=64f, R=16f, T=16f, B=36f
- Line stroke: 3f, dot radius: 5f
- Grid lines at 0%, 25%, 50%, 75%, 100%
- Height: 200dp

#### CalibrationChart
- Padding: L=48f, R=16f, T=16f, B=32f
- Dot radius: 6f–16f (scales with data count)
- Dot fill alpha: 0.25f, outline stroke: 2f
- Perfect calibration diagonal: dashed (8f, 6f pattern)
- Height: 200dp

#### ConfidenceChart
- Padding: L=36f, R=8f, T=16f, B=32f
- Bar corner radius: 4f, alpha: 0.85f
- Bar width: 60% of slot width
- Height: 180dp

## Screen Layouts

### Welcome

```text
Surface(fillMaxSize)
  Column(fillMaxSize, padding: 32dp, verticalArrangement: Center)
    Title: "Wisdometer" (32sp, Bold)
    Subtitle: bodyMedium, onSurfaceVariant
    Spacer(40dp)
    4x ConceptItem (title: titleMedium + body: bodyMedium)
    Spacer(weight=1)
    Button "Get started" (fillMaxWidth, RoundedCornerShape 8dp)
```

### Predictions (Main Tab)

```text
Scaffold
  TopBar: "Wisdometer" (headlineLarge, padding 16dp)
  FAB: FloatingActionButton (add prediction)
  Content:
    FilterChips row (LazyRow, spacing 8dp, padding h=16dp)
      StatusFilter: All | Open | Resolved
      Tag chips (dynamic)
    LazyColumn (spacing 8dp, padding h=16dp, v=4dp)
      PredictionCard per item (compact or expanded mode)
```

### Detail (Overlay)

```text
Scaffold
  TopBar: back + share + edit icons
  Column(verticalScroll, padding 16dp)
    Title (headlineMedium) + StatusBadge (aligned right)
    Description (bodyMedium, if present)
    Date info (created, updated)
    ProbabilityBar list (full-size)
    Tags display
    Button "Set Outcome" (primary, RoundedCornerShape 8dp)
    OutlinedButton "Delete Prediction"
    Dialogs: outcome selector, delete confirmation (AlertDialog)
```

### Edit / Create (Overlay)

```text
Scaffold
  TopBar: close + title + save icon
  Column(verticalScroll, padding 16dp)
    OutlinedTextField "Title" (single-line)
    OutlinedTextField "Description" (min 2 lines)
    Options section:
      Row per option: Label (weight 1f) + Probability (72dp width) + Delete icon
      Button "+ Add option"
      Sum indicator (green at 100%, red otherwise)
    OutlinedTextField "Tags" (comma-separated, single-line)
    Date buttons: Start / End (RoundedCornerShape 8dp)
    DatePickerDialog (Material3)
```

### Profile (Main Tab)

```text
Column(verticalScroll, padding 16dp)
  Title: "Profile" (headlineLarge)
  Accuracy card (Row):
    AccuracyDonut (72dp)
    Column: accuracy %, Brier score, prediction counts
  2x2 StatTile grid (Total, Resolved, Open, Avg Confidence)
  Accuracy chart card (toggle: "Over time" / "By count", height 200dp)
  Calibration chart card (height 200dp + description)
  Confidence distribution card (height 180dp + description)
  Tag accuracy breakdown (rows: tag + accuracy %)
  OutlinedButton "Share Stats" (full-width)
```

### Settings (Main Tab)

```text
Column(fillMaxSize, padding 16dp)
  Title: "Settings" (headlineLarge)
  Row: "Compact mode" + Switch
  HorizontalDivider (padding v=8dp)
  Row: "Notifications" + Switch
  HorizontalDivider
  OutlinedButton "Export JSON" (full-width, RoundedCornerShape 8dp)
  OutlinedButton "Import JSON" (full-width, RoundedCornerShape 8dp)
  Status message (auto-dismiss 3s)
```

## Navigation

- **HorizontalPager**: 3 main tabs (Predictions, Profile, Settings) with swipe
- **NavigationBar**: Bottom tab bar (hidden on overlay screens)
- **NavHost**: Overlay routes for Detail and Edit screens
- **Welcome screen**: Gated by `welcome_seen` key in SharedPreferences

## Label Casing

| Context | Casing | Examples |
|---------|--------|----------|
| Screen titles | Sentence case | "Profile", "Settings" |
| App name | Proper noun | "Wisdometer" |
| Section headers | Sentence case | "Accuracy over time", "Calibration" |
| Buttons | Sentence case | "Get started", "Export JSON", "Set Outcome" |
| Form labels | Sentence case | "Title", "Description (optional)" |
| Dialog titles | Sentence case | "Select Outcome", "Brier Score" |
| Dialog buttons | Sentence case | "Cancel", "Delete", "OK" |
| Status badges | ALL CAPS | "OPEN", "RESOLVED" |
| Filter chips | Capitalized | "All", "Open", "Resolved" |
| Chart toggles | Sentence case | "Over time", "By count" |
| Settings items | Sentence case | "Compact mode", "Notifications" |

## Common Patterns

| Pattern | Value |
|---------|-------|
| Screen padding | 16dp |
| Card elevation | 2dp |
| Card corner radius | 12dp |
| Card containerColor | MaterialTheme.colorScheme.surface (explicit) |
| Button shape | RoundedCornerShape(8dp) (explicit, not from theme) |
| Section spacing | 16dp |
| Item spacing | 8dp |
| Metadata style | labelSmall + onSurfaceVariant |
| Chart canvas heights | 180–200dp |
| Probability input width | 72dp |
| AccuracyDonut size | 72dp |
| Stat tile value | 28sp Bold |
