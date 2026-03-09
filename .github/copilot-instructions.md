# GitHub Copilot Instructions for BadArt

BadArt is an Android drawing app written in Kotlin. Users draw on a canvas and post their artwork to a shared feed where others can guess what was drawn.

## Project Structure

- `app/src/main/java/com/example/badart/` — main source code
  - `ui/` — Fragment-based UI (DrawFragment, FeedFragment, ProfileFragment, etc.)
  - `views/` — Custom views (DrawingView, ColorWheelView, ColorValueView) and the Tool enum
  - `viewmodel/` — SharedViewModel connecting UI to Firebase backend
  - `model/` — Data models (Post, User)
  - `util/` — Helpers (SoundManager, GameConstants, UiUtils)
- `app/src/main/res/` — Android resources (layouts, drawables, strings, etc.)

## Drawing Tools

Drawing tools are defined in `views/Tool.kt` as an enum. Each tool is handled in `DrawingView.kt` and wired to UI buttons in `DrawFragment.kt` and `fragment_draw.xml`.

Current tools: `BRUSH`, `FILL`, `ERASER`, `ANTIGRAVITY`

- **BRUSH** — standard freehand drawing stroke
- **FILL** — flood-fill a region with the selected colour
- **ERASER** — draws in white to erase
- **ANTIGRAVITY** — draws a mirrored stroke reflected across the horizontal centre of the canvas, creating a symmetrical antigravity effect

## Tech Stack

- Kotlin, Android SDK (minSdk 26)
- Jetpack Navigation, ViewBinding, Material Components
- Firebase (Firestore + Storage) via the Google Services plugin
- Coroutines for async flood-fill

## Conventions

- Fragments use ViewBinding (`FragmentXxxBinding.bind(view)` in `onViewCreated`)
- Tool selection is handled by `selectTool(button, Tool)` in DrawFragment
- Sound effects are played via `SoundManager` before tool/action calls
- New drawables follow the `ic_` naming convention and use vector XML
