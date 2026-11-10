package com.mahiuddin.magicstory

import com.mahiuddin.magicstory.domain.model.ExportResolution
import com.mahiuddin.magicstory.domain.model.MagicStoryEditorState
import com.mahiuddin.magicstory.domain.model.TemplateElement
import com.mahiuddin.magicstory.domain.model.TemplateManifest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CanvasViewportStabilityTest {

    private val sampleTemplate = TemplateManifest(
        schemaVersion = 1,
        id = "test_template",
        title = "Canvas Stability Test Template",
        category = "General",
        aspectRatio = "9:16",
        elements = listOf(
            TemplateElement.Text(
                id = "text_1",
                zIndex = 1,
                x = 0.1f, y = 0.1f, width = 0.8f, height = 0.2f,
                defaultText = "Test Text"
            ),
            TemplateElement.Shape(
                id = "shape_1",
                zIndex = 2,
                x = 0.2f, y = 0.3f, width = 0.5f, height = 0.3f,
                shapeType = "RECTANGLE"
            ),
            TemplateElement.PhotoSlot(
                id = "photo_1",
                zIndex = 3,
                x = 0.1f, y = 0.6f, width = 0.8f, height = 0.3f
            )
        )
    )

    @Test
    fun selectingElements_doesNotAlterExportResolutionOrAspectRatio() {
        var state = MagicStoryEditorState(template = sampleTemplate)
        val initialRatio = state.exportResolution.width.toFloat() / state.exportResolution.height.toFloat()

        // 1. Select Text
        state = state.copy(selectedElementId = "text_1")
        assertEquals("text_1", state.selectedElementId)
        assertEquals(ExportResolution.STORY, state.exportResolution)
        assertEquals(initialRatio, state.exportResolution.width.toFloat() / state.exportResolution.height.toFloat(), 0.0001f)

        // 2. Select Shape
        state = state.copy(selectedElementId = "shape_1")
        assertEquals("shape_1", state.selectedElementId)
        assertEquals(initialRatio, state.exportResolution.width.toFloat() / state.exportResolution.height.toFloat(), 0.0001f)

        // 3. Select Image/PhotoSlot
        state = state.copy(selectedElementId = "photo_1")
        assertEquals("photo_1", state.selectedElementId)
        assertEquals(initialRatio, state.exportResolution.width.toFloat() / state.exportResolution.height.toFloat(), 0.0001f)

        // 4. Deselect Element
        state = state.copy(selectedElementId = null)
        assertNull(state.selectedElementId)
        assertEquals(initialRatio, state.exportResolution.width.toFloat() / state.exportResolution.height.toFloat(), 0.0001f)
    }

    @Test
    fun explicitResolutionChange_updatesAspectRatioOnlyWhenRequested() {
        var state = MagicStoryEditorState(template = sampleTemplate)
        val storyRatio = 1080f / 1920f
        assertEquals(storyRatio, state.exportResolution.width.toFloat() / state.exportResolution.height.toFloat(), 0.0001f)

        // Change to Square Preset (1:1)
        state = state.copy(exportResolution = ExportResolution.SQUARE)
        val squareRatio = 1080f / 1080f
        assertEquals(squareRatio, state.exportResolution.width.toFloat() / state.exportResolution.height.toFloat(), 0.0001f)

        // Selecting element in Square mode preserves Square ratio
        state = state.copy(selectedElementId = "text_1")
        assertEquals(squareRatio, state.exportResolution.width.toFloat() / state.exportResolution.height.toFloat(), 0.0001f)
    }
}
