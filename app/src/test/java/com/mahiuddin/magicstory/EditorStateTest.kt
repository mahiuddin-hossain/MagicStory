package com.mahiuddin.magicstory

import com.mahiuddin.magicstory.domain.model.EditorSnapshot
import com.mahiuddin.magicstory.domain.model.MagicStoryEditorState
import com.mahiuddin.magicstory.domain.model.TemplateElement
import com.mahiuddin.magicstory.domain.model.TemplateManifest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorStateTest {

    private val sampleTemplate = TemplateManifest(
        schemaVersion = 1,
        id = "test_template",
        title = "Sample Template",
        category = "Fashion",
        elements = listOf(
            TemplateElement.Text(
                id = "title_text",
                zIndex = 1,
                x = 0.1f, y = 0.1f, width = 0.8f, height = 0.2f,
                defaultText = "DEFAULT TITLE"
            )
        )
    )

    @Test
    fun getTextValue_returnsEditedValueIfPresentElseDefault() {
        val state = MagicStoryEditorState(
            template = sampleTemplate,
            editedTexts = mapOf("title_text" to "CUSTOM TITLE")
        )

        assertEquals("CUSTOM TITLE", state.getTextValue("title_text", "DEFAULT TITLE"))
        assertEquals("DEFAULT SUBTITLE", state.getTextValue("subtitle_text", "DEFAULT SUBTITLE"))
    }

    @Test
    fun undoAndRedo_managesHistorySnapshotsCorrectly() {
        val snap0 = EditorSnapshot(emptyMap(), emptyMap())
        val snap1 = EditorSnapshot(mapOf("title_text" to "STEP 1"), emptyMap())
        val snap2 = EditorSnapshot(mapOf("title_text" to "STEP 2"), emptyMap())

        val state = MagicStoryEditorState(
            template = sampleTemplate,
            editedTexts = mapOf("title_text" to "STEP 2"),
            history = listOf(snap0, snap1, snap2),
            historyIndex = 2
        )

        assertTrue(state.canUndo)
        assertFalse(state.canRedo)

        val prevIndex = state.historyIndex - 1
        val prevSnap = state.history[prevIndex]
        val undoneState = state.copy(
            editedTexts = prevSnap.editedTexts,
            historyIndex = prevIndex
        )

        assertEquals("STEP 1", undoneState.getTextValue("title_text", "DEFAULT TITLE"))
        assertTrue(undoneState.canUndo)
        assertTrue(undoneState.canRedo)
    }
}
