# BottomShelfer for Android

A customizable slide-up bottom sheet for Android. Supports arbitrary height
detents, a draggable grabber, optional dimming scrim, keyboard avoidance,
rotation, scroll view coordination, and full layout configuration.

## Requirements

- Android 5.0+ (API 21+)
- Kotlin 1.9+

## Installation

Add the library module to your project or include as a dependency:

```kotlin
// settings.gradle.kts
include(":bottomshelfer")
```

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(project(":bottomshelfer"))
}
```

## Quick start

```kotlin
val sheet = BottomShelferSheet(context)
sheet.setDetents(listOf(
    BottomShelferDetent.medium(context),
    BottomShelferDetent.large(context)
))
sheet.setSelectedDetentIndex(0)
sheet.addContentView(myContentView)

val dialog = BottomShelferDialog(context, sheet)
dialog.show()
```

## Features

### Detents

Predefined or custom-height snap points:

```kotlin
sheet.setDetents(listOf(BottomShelferDetent.medium(context), BottomShelferDetent.large(context)))
sheet.setDetents(listOf(BottomShelferDetent.custom(320)))
sheet.setDetents(BottomShelferDetent.detentsForContentHeight(420, context))
sheet.setSelectedDetentIndex(1)
```

Factory methods on `BottomShelferDetent`:
- `small(context)` — 25% of screen height
- `medium(context)` — 50% of screen height
- `large(context)` — 90% of screen height
- `custom(height)` — explicit pixel height
- `detentsForContentHeight(height, context)` — auto-generates small/medium/large

### Grabber pill

Size, offset, and corner radius are configurable via `BottomShelferLayoutConfig`:

```kotlin
sheet.config = BottomShelferLayoutConfig(
    grabberPillWidthDp = 56,
    grabberPillHeightDp = 6,
    grabberPillCornerRadiusDp = 3f,
)
```

The pill animates on drag — scales to 1.3x, fades to 0.6 alpha. Set its size to
`0` to hide while keeping the drag gesture active.

### Dimming scrim

Optional semi-transparent backdrop. Tap behavior controlled by `dismissOnHide`:

```kotlin
sheet.config = sheet.config.copy(isDimmingEnabled = false)
sheet.config = sheet.config.copy(
    dimmingColor = 0x66000000.toInt()
)
```

### Drag & scroll coordination

The sheet coordinates with embedded scroll views (RecyclerView, ScrollView,
NestedScrollView). When the scroll view is pinned to the top, a downward drag
transfers control to the sheet:

```kotlin
sheet.config = sheet.config.copy(allowGrabbingNonScrollViews = true)
```

Disable dragging entirely with `isDraggingEnabled = false`.

### Keyboard avoidance

The sheet's dialog window uses `SOFT_INPUT_ADJUST_RESIZE` so it naturally
adjusts when the keyboard appears.

### Callbacks

```kotlin
sheet.callback = object : BottomShelferCallback {
    override fun onDismiss() { /* sheet dismissed */ }
    override fun onGrabberDragBegan() { /* grabber drag started */ }
    override fun onGrabberDragEnded() { /* grabber drag ended */ }
    override fun onContentDragBegan() { /* content drag started */ }
    override fun onContentDragEnded() { /* content drag ended */ }
    override fun onDetentChanged(index: Int, height: Int) {
        /* snapped to detent index at height */
    }
}
```

### Programmatic snapping

```kotlin
sheet.snapToHeight(320)
```

### Rotation

The sheet re-derives its detents when the configuration changes (rotation,
multi-window resize).

### Custom layout

Override defaults through `BottomShelferLayoutConfig`:

| Property | Default | Description |
|---|---|---|
| `maxSheetWidthDp` | 430 | Clamps sheet width on tablets |
| `maxHeightFraction` | 0.9 | Caps sheet height as fraction of container |
| `grabberHitAreaHeightDp` | 44 | Height of the draggable band |
| `grabberPillWidthDp` / `grabberPillHeightDp` | 36 / 5 | Pill dimensions |
| `grabberPillBottomOffsetDp` | 12 | Distance from sheet edge to pill |
| `grabberPillCornerRadiusDp` | 2.5 | Pill corner radius |
| `cornerRadiusDp` | 20 | Sheet top corner radius |

```kotlin
sheet.config = BottomShelferLayoutConfig(
    maxSheetWidthDp = 500,
    maxHeightFraction = 0.6f,
    cornerRadiusDp = 28f,
)
```

### Compose interop

Embed Jetpack Compose content inside a bottom sheet via `ComposeView`:

```kotlin
val composeView = ComposeView(context).apply {
    setContent { MyComposeContent() }
}
sheet.addContentView(composeView)
```

## License

MIT — see [LICENSE](LICENSE).

## Author

**jonikay89** — [@jonikay89](https://github.com/jonikay89)
