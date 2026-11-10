# MagicStory 🎨✨

**MagicStory** is a modern, high-performance Android graphic editor and social media template customization application inspired by PixelLab and Canva. Built with 100% Kotlin, Jetpack Compose, Clean Architecture + MVVM, and native Android Canvas graphics rendering.

---

## 🌟 Key Features & Capabilities

### 🎨 PixelLab-Inspired Graphic Editor
* **Multi-Layer Stack Management (`EditorLayersPanel`):** Z-index layer ordering, layer lock/unlock, visibility toggles, layer selection, and element duplication/deletion.
* **Vector Shapes Library (`EditorVectorShapeManager`):** Insertion and customization of 10+ vector shapes including Rectangles, Rounded Rectangles, Circles, Ellipses, Triangles, Diamonds, Pentagons, Hexagons, Octagons, Stars, and Custom Polygons.
* **Typography & Text Tools:** Text content editing, font family selector (Roboto, Serif, Cursive, Monospace, Impact, Inter, etc.), font sizing, letter/line spacing, color & fill, and background highlighting.
* **Canvas Backgrounds & Textures:** Custom background colors, gradient fills, textures, and canvas aspect ratio presets (`9:16 Story`, `4:5 Portrait`, `1:1 Square`, `16:9 Landscape`).
* **Photo Slot & Frame Replacement:** Image picker integration for inserting and replacing photos within frames and slots.

### 📐 Precision Transforms & Gestures
* **Touch Transformations (`TemplateCanvasPreview`):** Direct canvas gesture handling for moving, corner/edge resizing, rotation stem rotating, and horizontal/vertical flipping.
* **Hit-Testing Engine:** Accurate inverse coordinate transformation matrix for selecting and manipulating rotated text, photo, and vector elements.
* **Snapping Guides:** Automatic canvas center vertical and horizontal snapping guide lines.
* **Illustrator-Style Precision Bounding Box (`EditorTransformPrecisionDialog`):** Exact numeric controls for $(X, Y)$ position, width, height, and rotation angle.

### 🖌️ Advanced Styling & Effects
* **Color Palette & Eyedropper:** 2D HSV color picker, preset swatches, hex color input, and an interactive real-time canvas color-sampling eyedropper loupe.
* **Custom Gradient Builder:** Linear and radial gradient fill creation with angle controls.
* **Stroke Customization (`EditorStrokeAdvancedPanel`):** Adjustable stroke width, line patterns (Solid, Dashed, Dotted), line caps (Butt, Round, Square), line joins (Miter, Round, Bevel), and stroke opacity.
* **Corner Radius Controls (`EditorCornerRadiusPanel`):** Uniform linked corner radius vs. unlinked individual corner radii ($TL, TR, BR, BL$).
* **Shape Geometry Controls (`EditorGeometryPanel`):** Dynamic polygon side count ($3..16$) and star point count with inner radius ratios.
* **Layer Effects:** Drop shadow, outer glow, inner glow, and element opacity sliders.

### 💾 High-Resolution Export Engine
* **Format Support:** High-DPI export to `JPEG`, `PNG`, and `WEBP` formats.
* **Resolution Scaling Multipliers:** Export scaling options up to 4x resolution.
* **Clean Artwork Export:** Asynchronous background rendering (`ImageExporter`) strictly excluding editor overlays, transform handles, selection outlines, and snapping guides.
* **Gallery Integration:** Automatic MediaStore saving for photo gallery access.

---

## 🛠️ Architecture & Tech Stack

* **UI Toolkit:** Jetpack Compose (100% Declarative UI with Material 3).
* **Architecture:** Clean Architecture + MVVM (Model-View-ViewModel) + Single Source of Truth `StateFlow`.
* **State Management:** Immutable `MagicStoryEditorState` with full undo/redo stack (`EditorSnapshot`).
* **Graphics Engine:** Native Android `Canvas` rendering engine (`TemplateRenderer`).
* **Concurrency:** Kotlin Coroutines & `StateFlow`.
* **Build System:** Gradle (Kotlin DSL / Groovy).

---

## 📁 Project Directory Structure

```
com.mahiuddin.magicstory/
├── data/                       # Data Layer (Asset Data Source, DTOs, Repositories)
│   ├── local/AssetTemplateDataSource.kt
│   ├── model/TemplateDtos.kt
│   └── repository/TemplateRepositoryImpl.kt
├── domain/                     # Domain Layer (Models, Interfaces, State)
│   ├── model/EditorState.kt
│   ├── model/TemplateElement.kt
│   ├── model/TemplateManifest.kt
│   └── repository/TemplateRepository.kt
├── navigation/                 # Jetpack Compose Navigation
│   └── NavGraph.kt
├── template/                   # Graphics & Rendering Engine
│   ├── export/ImageExporter.kt
│   ├── parser/TemplateParser.kt
│   ├── renderer/TemplateRenderer.kt
│   └── validator/TemplateValidator.kt
├── ui/                         # Presentation Layer (Composables, ViewModels)
│   ├── components/             # Reusable UI Components
│   │   ├── CommonUi.kt
│   │   ├── MinimalSlider.kt
│   │   └── TemplateCanvasPreview.kt
│   ├── editor/                 # Editor Sub-modules & Panels
│   │   ├── EditorCanvasManager.kt
│   │   ├── EditorCornerRadiusPanel.kt
│   │   ├── EditorDialogs.kt
│   │   ├── EditorGeometryPanel.kt
│   │   ├── EditorLayersPanel.kt
│   │   ├── EditorScreen.kt
│   │   ├── EditorStrokeAdvancedPanel.kt
│   │   ├── EditorVectorShapeManager.kt
│   │   └── EditorViewModel.kt
│   ├── home/                   # Home Screen & Templates Feed
│   │   ├── HomeScreen.kt
│   │   └── HomeViewModel.kt
│   └── theme/                  # Material 3 Styling & Typography
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── util/                       # Helpers & Media Store Utilities
    ├── BitmapUtils.kt
    └── MediaStoreUtils.kt
```

---

## 🧪 Unit Tests

Run local unit tests:
```bash
./gradlew :app:testDebugUnitTest
```
Includes test suites for canvas viewport stability, editor state history, and template parsing:
* `CanvasViewportStabilityTest.kt`
* `EditorStateTest.kt`
* `TemplateParserTest.kt`

---

## 🚀 Building & Running

### Requirements
* **Android Studio:** Ladybug (2024.2.1) or newer.
* **JDK:** 17 or higher.
* **Minimum SDK:** API 24 (Android 7.0).
* **Target SDK:** API 35 (Android 15).

### Build Commands
```bash
# Debug APK Assembly
./gradlew :app:assembleDebug

# Run Unit Tests
./gradlew :app:testDebugUnitTest
```

---

## 📄 License
Designed and developed by **Mahiuddin**. Distributed under the MIT License.
