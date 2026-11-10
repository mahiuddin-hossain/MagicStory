# MagicStory — EditorScreen Architecture & Feature Documentation

> **Overview:** `EditorScreen.kt` is the core visual editor of MagicStory. It provides an intuitive, Photoshop & Illustrator-inspired, Canva-style mobile graphic design environment. It combines direct canvas manipulation, a data-driven template engine, a native vector shape library, advanced typography, custom gradients, and Photoshop-grade layer effects.

---

## 📋 Table of Contents
1. [Canvas Workspace & Morphing System](#1-canvas-workspace--morphing-system)
2. [Secondary Quick Toolbar Bar](#2-secondary-quick-toolbar-bar)
3. [Vector Shape Tools & Library](#3-vector-shape-tools--library)
4. [Illustrator-Style Transform & Precision Panel](#4-illustrator-style-transform--precision-panel)
5. [Fill & Color Management Engine](#5-fill--color-management-engine)
6. [Advanced Stroke & Outline System](#6-advanced-stroke--outline-system)
7. [Corner Radius & Geometry System](#7-corner-radius--geometry-system)
8. [Photoshop-Grade Layer Effects Engine](#8-photoshop-grade-layer-effects-engine)
9. [Text & Typography System](#9-text--typography-system)
10. [PixelLab-Style Layer Drawer & Reordering](#10-pixellab-style-layer-drawer--reordering)
11. [Photo Slot & Image Frame System](#11-photo-slot--image-frame-system)
12. [High-Res Image Export System](#12-high-res-image-export-system)
13. [Work Process & State Flow Architecture](#13-work-process--state-flow-architecture)

---

## 1. Canvas Workspace & Morphing System
- **Responsive Canvas Preview (`TemplateCanvasPreview`)**: Renders data-driven templates in real-time using native `android.graphics.Canvas` and `android.graphics.Path`.
- **Dynamic Aspect Ratio Morphing**: Smooth spring physics animations (`animateFloatAsState`) transition the canvas aspect ratio when switching resolution presets (`9:16 Story`, `4:5 Portrait`, `1:1 Square`, `16:9 Landscape`) without distorting inner elements.
- **Interactive Eyedropper Layer**: Touch-anywhere loupe magnifier overlay (`TemplateRenderer.sampleColorAt`) samples precise pixel colors directly from the rendered canvas bitmap.
- **Snapping Guide Lines**: Snaps element centers to vertical/horizontal canvas middle axes ($X = 0.5$, $Y = 0.5$) with dashed red alignment guidelines.

---

## 2. Secondary Quick Toolbar Bar
Positioned directly below the Top App Bar, this horizontal scrollable toolbar provides instant access to core creation and canvas management tools:
- 🔲 **Insert Vector Shape Library (`ShapeSelectionDialog`)**: Opens the categorized vector shapes panel.
- 🖼️ **Insert Image (`AddPhotoAlternate`)**: Opens system photo picker to insert a new photo frame.
- 🖼️ **Canvas Aspect Ratio**: Resolution preset selector dropdown (`9:16`, `4:5`, `1:1`, `16:9`).
- 🥞 **Layers Drawer Button**: Opens the PixelLab-style right-side layer stack drawer.
- ↩️ **Undo (Backward)**: Step back in editing history.
- ↪️ **Redo (Forward)**: Step forward in editing history.

---

## 3. Vector Shape Tools & Library
MagicStory renders all shapes as **native, resolution-independent vector paths** (`VectorShapePathFactory`).

### Supported Shape Categories (`ShapeSelectionDialog`):
- **Basic Shapes**:
  - ⬛ Rectangle
  - 🔲 Rounded Rectangle
  - ⏹️ Square
  - 🔴 Circle / Ellipse
  - 🔺 Triangle (Equilateral)
  - 📐 Right Triangle
  - 🔷 Diamond
  - ⬟ Pentagon
  - ⬢ Hexagon
  - 🛑 Octagon
  - ⭐ Star (3 to 16 points)
  - ⬡ Polygon (3 to 16 sides)
- **Lines & Arrows**:
  - ➖ Straight Line
  - ➡️ Arrow (Single Head)
  - ↔️ Double Arrow (Dual Heads)
  - ┅ Dashed Line
  - ┈ Dotted Line
- **Custom Paths**:
  - ✏️ Custom Polygon
  - ✒️ Freeform Path
  - 🌀 Bezier Path

---

## 4. Illustrator-Style Transform & Precision Panel
- **Direct Canvas Manipulation**:
  - **Move**: Drag inside the selected shape bounding box.
  - **Scale**: Drag corner or edge handles.
  - **Rotate**: Drag top rotation stem ($0^\circ - 360^\circ$ with $45^\circ$ snap angles).
  - **Constrained / Proportional Scaling**: Ratio lock toggle (`isConstrained`) maintains aspect ratio anchored to the opposite corner.
- **Transform Precision Dialog (`TransformPrecisionDialog`)**:
  - Numeric input for Position X and Position Y (Normalized $0.0 - 1.0$).
  - Numeric input for Dimensions Width and Height.
  - Rotation Angle slider ($0^\circ - 360^\circ$).
  - Skew / Shear X & Y sliders ($-45^\circ$ to $+45^\circ$).
  - Proportional Ratio Lock Toggle (`isConstrained`).
  - Flip Horizontal (⇄) and Flip Vertical (⇅) actions.

---

## 5. Fill & Color Management Engine
- **Fill Types**: `Solid Fill`, `Custom Gradient Fill`, `No Fill / Transparent`.
- **Advanced 2D HSV Color Picker (`AdvancedColorPickerDialog`)**:
  - Photorealistic 2D Saturation / Value Canvas Box with circular touch indicator.
  - 1D Rainbow Hue Slider Bar.
  - 1D Alpha Opacity Bar.
  - Live Hex Input Field with instant parsing.
  - Canvas Eyedropper button.
  - Swatch Saver (Adds current color to palette swatches).
- **Custom Gradient Builder (`CustomGradientPickerDialog`)**:
  - Gradient Types: `Linear`, `Radial`, `Sweep`.
  - Color Stop Rail: Add / Delete stops, pick individual stop colors.
  - Direction Angle Slider ($0^\circ - 360^\circ$).
  - Color Stop Reversal (⇄).
  - Tile Modes: `Clamp`, `Mirror`, `Repeat`.
- **Swap Fill & Stroke (⇄)**: Instantly swaps fill color and stroke border color.

---

## 6. Advanced Stroke & Outline System
Configurable via `StrokeAdvancedPanel`:
- **Stroke Enable / Disable Toggle**.
- **Stroke Width Slider**: $0 - 40\text{ dp}$.
- **Stroke Style**: `Solid`, `Dashed` (`DashPathEffect(16dp, 10dp)`), `Dotted` (`DashPathEffect(4dp, 8dp)`).
- **Stroke Cap**: `Butt`, `Round`, `Square`.
- **Stroke Join**: `Miter`, `Round`, `Bevel`.
- **Stroke Opacity**: $0\% - 100\%$.
- **Color Selection**: Custom Color Picker, Eyedropper, Swatches.

---

## 7. Corner Radius & Geometry System
Configurable via `CornerRadiusPanel` & `GeometryPanel`:
- **Rectangle Corner Radius**:
  - **Uniform Mode**: Single radius slider ($0 - 80\text{ dp}$).
  - **Unlinked Mode**: 4 individual sliders for `Top-Left`, `Top-Right`, `Bottom-Right`, and `Bottom-Left` corner radii.
- **Polygon Geometry**:
  - Polygon side count slider ($3 - 16\text{ sides}$).
- **Star Geometry**:
  - Star point count slider ($3 - 16\text{ points}$).
  - Star inner radius ratio slider ($10\% - 90\%$).

---

## 8. Photoshop-Grade Layer Effects Engine
Configurable via `ActiveSubTool.EFFECTS`:

### A. Drop Shadow 👥
- **Photoshop Light Source Angle ($\theta$)**: $0^\circ - 360^\circ$ with quick presets ($45^\circ$, $90^\circ$, $135^\circ \text{ PS}$, $180^\circ$, $270^\circ$, $315^\circ$).
- **Trigonometric Offset Math**:
  $$\text{dx} = \text{distance} \cdot \cos(\theta)$$
  $$\text{dy} = \text{distance} \cdot \sin(\theta)$$
- **Distance Slider**: $0 - 100\text{ dp}$.
- **Blur Softness Radius**: $0 - 60\text{ dp}$.
- **Shadow Spread**: $0 - 30\text{ dp}$ (Expands solid shadow mass before blur falloff).
- **Opacity Slider**: $0\% - 100\%$.

### B. Outer Glow ✨
- **Volumetric Dual-Pass Halo**: Soft outer halo + intense core spread layer.
- **Glow Radius**: $0 - 80\text{ dp}$.
- **Glow Spread**: $0 - 40\text{ dp}$.
- **Glow Opacity**: $0\% - 100\%$.

### C. Inner Glow 🌟
- **Precision Edge Clipping (`canvas.clipPath()`)**: Guarantees inner glow remains strictly inside element borders without bleeding.
- **Inward Blur Radius**: $0 - 50\text{ dp}$.
- **Choke / Expansion**: $0 - 30\text{ dp}$.
- **Glow Opacity**: $0\% - 100\%$.

---

## 9. Text & Typography System
- **Inline Text Editing Dialog**: Tap "Edit" to modify text content.
- **Font Family Selector**: `Roboto Sans`, `Editorial Serif`, `Dancing Script`, `Space Mono`, `Casual Sans`, `Impact Heavy`, `Fashion Condensed`, `Montserrat Light`, `Inter Medium`.
- **Font Size Slider**: $0 - 500\text{ sp}$.
- **Text Spacing Controls**: Letter Spacing ($0.0 - 0.4$) & Line Spacing ($0.8 - 2.0$).
- **Text Background Pill**: Highlight pill with custom color.
- **Text Case Switcher**: `UPPERCASE`, `lowercase`, `Normal`.
- **Typography Alignment & Weight**: Left / Center / Right alignment & Normal / Bold weight toggles.

---

## 10. PixelLab-Style Layer Drawer & Reordering
- **Right Slide-Over Navigation Drawer**: Displays all visible layers sorted by Z-Index.
- **Transparent Checkerboard Thumbnails**: Live element thumbnails rendered using `TemplateRenderer.renderElementThumbnail`.
- **6-Dot Drag Handles (`:::`)**: Smooth drag-to-reorder layer stack with floating elevation ($12\text{ dp}$) and scale ($1.04\times$) animations.
- **Quick Layer Matrix Actions**:
  - 🔒 **Lock / Unlock**: Prevents accidental canvas gestures.
  - 👁️ **Hide / Show**: Toggles element visibility.
  - 🗑️ **Delete**: Removes element from composition.

---

## 11. Photo Slot & Image Frame System
- **System Photo Picker Integration**: Launch system media picker (`PickVisualMediaRequest`).
- **Replace Photo**: Swaps image asset in any photo slot or photo card.
- **Cover / Contain Fit**: Maintains aspect ratio without squishing/stretching pixels.
- **Transform & Flip**: Flip Horizontal (⇄), Flip Vertical (⇅), Rotation, Opacity, Layer Effects.

---

## 12. High-Res Image Export System
- **Export Settings Dialog**:
  - **Export Formats**: `JPEG`, `PNG`, `WEBP`.
  - **Resolution Scaling**: `1X` ($1080\text{p}$), `2X` ($2K$), `3X` ($3K$), `4X` ($4K$ Ultra-HD).
- **Background Thread Export (`ImageExporter.kt`)**: Asynchronously renders manifest and editor state to a full-resolution `Bitmap`, saves to MediaStore, and displays `ExportSuccessDialog` with local file URI.

---

## 13. Work Process & State Flow Architecture

```
                      +-----------------------------+
                      |     User Input / Gestures    |
                      +--------------+--------------+
                                     |
                                     v
                      +-----------------------------+
                      |       EditorScreen.kt       |
                      |  (Compose UI & Toolbar)     |
                      +--------------+--------------+
                                     |
                                     v
                      +-----------------------------+
                      |     EditorViewModel.kt      |
                      |  (StateFlow & History)      |
                      +--------------+--------------+
                                     |
                                     v
                      +-----------------------------+
                      |     MagicStoryEditorState   |
                      |  (EditorSnapshot Undo/Redo) |
                      +--------------+--------------+
                                     |
                                     v
                      +-----------------------------+
                      |    TemplateRenderer.kt      |
                      | (Canvas & Vector Path Engine|
                      +-----------------------------+
```

1. **User Interaction**: User taps a tool in the **Secondary Quick Toolbar**, drags on **`TemplateCanvasPreview`**, or opens a sub-tool panel in the contextual bottom panel.
2. **ViewModel Event**: UI invokes ViewModel functions (e.g., `addShapeElement`, `updateElementPosition`, `updateShapeTransform`, `updateDropShadow`).
3. **Immutable State & History**: `EditorViewModel` creates a new `MagicStoryEditorState` and pushes an `EditorSnapshot` into `history` for instant Undo / Redo support.
4. **Native Drawing Pipeline**: `TemplateCanvasPreview` recomposes and delegates rendering to `TemplateRenderer.renderToCanvas`.
5. **High-Res Output**: On export, `ImageExporter` uses `TemplateRenderer` to output a crisp, un-pixelated high-resolution image file.
