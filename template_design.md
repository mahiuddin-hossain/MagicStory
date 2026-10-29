# MagicStory --- Template Design & Implementation Rules

> **Purpose:** This document is the permanent design and engineering
> guide for creating, adding, reviewing, or modifying templates in
> MagicStory. Read and follow it **before** designing or implementing
> any template.
>
> **Core principle:** Templates are data-driven compositions rendered by
> the shared template engine. Do not create a new screen or a
> template-specific renderer for each design.

## 1. Required workflow before implementation

Before creating or modifying a template, the AI agent must:

1.  Read this document completely.
2.  Inspect the existing template JSON schema, renderer, editor state,
    asset/font loaders, preview path, and export path.
3.  Reuse existing supported element types and conventions wherever
    possible.
4.  Inspect the supplied reference image, if one is attached. Treat it
    as a visual reference, not as a flattened background to display as
    the finished template.
5.  Break the design into ordered layers and identify which parts are:
    -   editable text;
    -   replaceable photo slots;
    -   native-drawn shapes and effects;
    -   bundled fonts or optional image assets;
    -   non-editable decorative artwork, if any.
6.  Check whether the required features are already supported by the
    schema and renderer.
7.  If a required element type or rendering feature is missing,
    implement it as a **generic reusable capability** first. Do not add
    one-off conditions tied to a template ID.
8.  Create the JSON manifest and any genuinely necessary assets.
9.  Validate the manifest, render a preview, and compare it with the
    reference.
10. Test editing and high-resolution export before declaring the
    template complete.

Do not stop after writing JSON if the template cannot render correctly
in the actual app.

## 2. Product and visual direction

MagicStory is a minimal, polished Android story, poster, and
social-media design editor.

### App UI palette

Use the app's centralized theme tokens. The current intended visual
direction is:

-   Primary red: `#C91F2C`
-   Deep red: `#A91521`
-   Accent red: `#E53945`
-   Main background: `#FFFFFF`
-   Secondary background: `#FFF7F7`
-   Main text: `#211D1E`
-   Secondary text: `#777174`
-   Subtle border: `#F0E5E6`

These are **app UI** colors, not mandatory colors for every template. A
template should preserve the reference design's own palette.

### Design quality

-   Prefer clean composition, deliberate whitespace, strong hierarchy,
    and consistent alignment.
-   Use subtle shadows only when they communicate depth.
-   Avoid unnecessary decoration, random gradients, excessive rounded
    cards, and generic AI-generated filler.
-   Do not invent text, logos, URLs, or brand names that are not present
    in the brief or reference. If sample text is needed, use clearly
    editable placeholder content.
-   Keep text legible at the final export size.
-   Respect the visual intent of the reference while making elements
    genuinely editable where practical.

## 3. Template output requirements

Every new template should include:

1.  A unique, stable template ID.
2.  A supported `schemaVersion`.
3.  Canvas dimensions and intended format/aspect ratio.
4.  A descriptive title and category if supported by the app's metadata
    model.
5.  A valid ordered element list.
6.  Explicit editability for user-editable elements.
7.  Sensible default text and photo-slot values.
8.  Font and asset references that resolve locally.
9.  A thumbnail/preview asset if required by the existing catalog
    implementation; otherwise use the shared renderer to produce a
    preview.
10. A successful validation result.
11. A successful editor preview and export check.

Use the project's existing manifest schema as the source of truth. The
examples in this document are conceptual; **do not introduce unsupported
JSON properties** without updating the schema, parser, validator,
renderer, and tests.

## 4. Canvas and coordinate system

Use the existing template coordinate system. Unless the project schema
specifies otherwise, use normalized coordinates:

-   `x`, `y`: normalized top-left position in the range `0..1`.
-   `width`, `height`: normalized dimensions relative to the canvas.
-   `rotation`: degrees.
-   `zIndex`: drawing order; lower values render behind higher values.
-   `opacity`: normalized value from `0..1`.

Do not mix normalized values with pixel values in the same property.

Recommended initial canvas sizes:

  Format             Canvas size           Aspect ratio
  ---------------- ------------- ----------------------
  Story              1080 × 1920                   9:16
  Portrait post      1080 × 1350                    4:5
  Square post        1080 × 1080                    1:1
  Landscape post      1080 × 566   approximately 1.91:1

Use the format requested for the template. Do not change the reference
aspect ratio accidentally.

### Layout rules

-   Keep important text and content away from canvas edges.
-   Avoid accidental clipping of text, shadows, borders, and rotated
    elements.
-   Account for the full rotated bounds of an element, not only its
    unrotated rectangle.
-   Define rotation pivots consistently and use the same rule in preview
    and export.
-   Make overlapping relationships intentional and encode them with
    `zIndex`.
-   Use predictable scaling for font sizes, stroke widths, corner radii,
    and shadows.
-   If adapting a design to another aspect ratio, use an explicit layout
    strategy; never stretch the design non-uniformly.

## 5. Layer decomposition

Before writing JSON, create a layer plan. A suitable order for an
editorial poster might be:

1.  Background fill or gradient.
2.  Native-drawn background decoration.
3.  Large background typography.
4.  Main photo/card.
5.  Foreground overlays and decorative details.
6.  Captions and labels.
7.  Call-to-action component.

The actual order must follow the reference composition. A photo card may
need to appear behind a foreground heading or above another element.

For each visual object, decide whether it should be:

-   **Native element:** draw with Canvas, paths, shapes, gradients,
    borders, or text.
-   **Composite element:** a reusable component such as a photo card,
    badge, pill, or framed caption.
-   **Image asset:** only when the visual is complex, painterly,
    textured, or impractical to reproduce natively.
-   **User content slot:** replaceable photo or editable text.
-   **Bundled font:** when a particular typographic style is important
    and a properly licensed font is available.

Do not flatten the entire reference into one image if the user is
expected to replace photos or edit text.

## 6. Prefer native drawing; minimize image assets

The first version of MagicStory should work offline and should avoid
unnecessary PNG/WebP dependencies.

Prefer native rendering for:

-   solid and gradient backgrounds;
-   rectangles and rounded rectangles;
-   lines, borders, circles, and simple paths;
-   simple abstract geometric decoration;
-   text and labels;
-   pills and call-to-action shapes;
-   photo frames;
-   photo clipping and crop behavior;
-   basic shadows.

Use bundled raster or vector assets only when native drawing would be
impractical or materially reduce fidelity, for example:

-   intricate paper textures;
-   hand-drawn stickers;
-   complex torn-paper edges;
-   detailed illustrations;
-   photographic overlays.

If an asset is necessary:

-   keep it in the app's local assets/resources;
-   use a descriptive, stable filename;
-   reference it through the existing asset mechanism;
-   verify that the file exists and can be decoded;
-   include a fallback or a clear validation error;
-   avoid duplicating identical assets across templates;
-   verify licensing and redistribution rights.

Do not require cloud storage, a backend, remote URLs, or runtime API
calls for a built-in template.

## 7. Text and typography

Text should remain real text whenever the user may edit it.

For each text element, configure only properties supported by the
schema, such as:

-   default text;
-   font family or registered font key;
-   size;
-   weight;
-   color;
-   alignment;
-   line spacing;
-   maximum lines or wrapping behavior;
-   rotation;
-   editability.

Rules:

-   Use the actual font resource or a reliable fallback.
-   Never assume an arbitrary font name will resolve automatically.
-   Avoid text clipping and unintended line breaks.
-   Measure text using the same rendering metrics in preview and export.
-   Define how overflow is handled: wrap, shrink within a reasonable
    range, or clip according to the template specification.
-   Do not shrink text so far that it becomes unreadable.
-   Keep editable text associated with a stable element ID.
-   Do not render editable text as a PNG.
-   Ensure special characters and apostrophes are correctly encoded in
    JSON.
-   For logos or highly stylized lettering, use a properly licensed
    vector/image asset if native text cannot reproduce the required
    appearance.

## 8. Photo slots and photo cards

Photo slots must be independent editable regions, not just decorative
rectangles.

Each photo slot should define, using the existing schema:

-   stable element/slot ID;
-   normalized position and dimensions;
-   clipping shape or mask, if supported;
-   fit mode;
-   crop parameters or default crop policy;
-   rotation and transform behavior;
-   border/frame behavior;
-   editability.

### Crop behavior

For a `cover` fit, preserve the source image aspect ratio and scale
until the target region is filled. Crop overflow; never stretch the
image to fit.

For a `contain` fit, preserve the entire image and allow empty space
where necessary.

Use the same crop parameters in the editor preview and exported bitmap.
Replacing a photo must not change the frame geometry or shift unrelated
layers.

### Composite photo cards

A photo card may contain:

-   a background/frame;
-   a clipped photo slot;
-   a caption area;
-   editable caption text;
-   border and shadow.

Prefer a reusable composite component when multiple templates need the
same structure. Keep the component configurable through the manifest. Do
not hardcode behavior based on a specific template ID.

## 9. JSON schema and compatibility

-   Use the schema version already supported by the app.
-   Give every element a unique ID within the template.
-   Use stable IDs for editable fields and photo slots.
-   Keep JSON valid and properly escaped.
-   Use only documented, supported element types and properties.
-   Do not put Kotlin expressions, executable code, or arbitrary scripts
    in JSON.
-   Do not store absolute local filesystem paths.
-   Do not use remote asset URLs for built-in offline templates.
-   Avoid redundant or contradictory fields.
-   Set defaults explicitly when their omission could cause inconsistent
    rendering.
-   Preserve backward compatibility when adding schema capabilities.

If the current schema cannot express the design:

1.  Identify the missing generic capability.
2.  Update the data model and parser.
3.  Update validation.
4.  Implement reusable renderer support.
5.  Add tests.
6.  Only then use the new capability in the template.

Do not silently invent a property that the renderer ignores.

## 10. Shared renderer requirements

All templates must use the shared renderer for:

-   Home thumbnails where renderer-generated previews are used;
-   editor preview;
-   final export.

The renderer must:

-   render in zIndex order;
-   apply consistent transforms and rotation pivots;
-   clip photos correctly;
-   restore Canvas/graphics state after transformations;
-   resolve fonts and assets through the shared loaders;
-   scale dimensions consistently;
-   handle missing/invalid values safely;
-   avoid unnecessary bitmap decoding and allocations;
-   render independently of the editor controls and device system bars.

Do not use a screenshot of the UI as the final export. Export from the
template and editor state at the requested output resolution.

If the current renderer has a limitation, fix or extend it generically
rather than creating a separate renderer for this template.

## 11. Reference matching and visual QA

When a reference image is supplied:

1.  Inspect the complete composition.
2.  Identify the canvas ratio, margins, typography hierarchy, dominant
    colors, alignment, rotations, shadows, and overlaps.
3.  Estimate element bounds relative to the canvas.
4.  Reconstruct editable elements with native rendering where possible.
5.  Render the first preview.
6.  Compare the preview with the reference.
7.  Correct obvious differences in scale, spacing, crop, rotation, font
    weight, and z-order.
8.  Render again after corrections.
9.  Verify the final export as well as the on-screen preview.

Do not claim pixel-perfect similarity without comparing rendered output.

A useful comparison checklist:

-   Is the background color/gradient close to the reference?
-   Are headline sizes and positions correct?
-   Does the photo occupy the correct area?
-   Is the photo crop correct?
-   Do frame, caption, and photo rotate together as intended?
-   Are foreground and background layers ordered correctly?
-   Are shadows subtle and in the correct direction?
-   Is all text readable and unclipped?
-   Does the CTA fit within the canvas?
-   Does the export match the preview?

If an exact font or complex decorative element is unavailable, preserve
the overall composition and clearly report the limitation rather than
pretending it is exact.

## 12. Editor compatibility

Every new template must work with the existing editor.

-   Editable text must appear in the text-editing interface.
-   Editable photo slots must appear in the photo replacement flow.
-   Stable element IDs must connect controls to the correct element.
-   Editing must update the preview immediately.
-   Template defaults must remain immutable.
-   User changes must live in editor state, not be written back into the
    original bundled manifest.
-   Reset must restore defaults.
-   Export must use the current editor state.
-   Non-editable decorative elements must remain unchanged during normal
    edits.

Do not add UI controls for capabilities that have not been implemented.

## 13. Validation and tests

Before declaring a template complete:

-   Parse its JSON with the production parser.
-   Run the existing schema validator.
-   Check duplicate IDs and unsupported element types.
-   Check that every referenced font and asset exists.
-   Check that normalized bounds and dimensions are sensible.
-   Render the template in the shared renderer.
-   Open it in the editor.
-   Replace every supported photo slot.
-   Edit every supported text field.
-   Verify reset behavior.
-   Export at the target resolution.
-   Confirm the output dimensions and inspect the saved image.
-   Check that no element is unintentionally missing, clipped,
    stretched, or reordered.
-   Run relevant unit tests and the project build where possible.

Do not report successful build/test results unless the commands actually
completed successfully. If a check cannot be run, state that explicitly.

## 14. Naming and file placement

Follow existing project conventions. Suggested paths:

-   Manifest: `app/src/main/assets/templates/<template_id>.json`
-   Fonts: `app/src/main/assets/fonts/` or `app/src/main/res/font/`
-   Preview: `app/src/main/assets/previews/<template_id>.webp` if
    required
-   Tests: the existing unit/instrumented test source sets

Use lowercase snake_case or the project's established ID convention. IDs
must be unique and stable; do not rename IDs casually after editor
support depends on them.

## 15. Adding a new template: required response from the agent

For each template task, the agent should provide a concise
implementation report containing:

1.  Template ID, category, and canvas dimensions.
2.  Layer breakdown and z-order.
3.  Elements that are native-drawn versus image/font assets.
4.  Editable text fields and photo slots.
5.  Files created or modified.
6.  Validation results.
7.  Preview/export verification results.
8.  Known visual differences or unsupported details.
9.  Confirmation that existing templates and generic renderer behavior
    were not unintentionally broken.

The agent should implement the template, not merely return this report.

## 16. Non-negotiable rules

-   Read this file before every template creation or modification task.
-   Use the shared JSON-driven renderer.
-   Prefer native drawing and editable text over flattened images.
-   Keep built-in templates local and offline-capable.
-   Do not create one-off template screens or template-ID-specific
    rendering branches.
-   Do not invent unsupported schema fields.
-   Do not ignore asset/font resolution errors.
-   Do not claim a preview or export was verified unless it was actually
    rendered and checked.
-   Do not break existing templates to support a new one.
-   Keep the implementation minimal, reusable, testable, and
    maintainable.

**Definition of done:** A template is complete only when its manifest is
valid, its assets resolve, it renders in the actual app, its supported
elements can be edited, and its high-resolution export correctly
reflects the current editor state.
