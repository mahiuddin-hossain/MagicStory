package com.mahiuddin.magicstory

import com.mahiuddin.magicstory.domain.model.TemplateElement
import com.mahiuddin.magicstory.template.parser.TemplateParser
import com.mahiuddin.magicstory.template.validator.TemplateValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TemplateParserTest {

    @Test
    fun parseValidTemplateJson_returnsSuccessManifest() {
        val json = """
            {
              "schemaVersion": 1,
              "id": "test_001",
              "title": "Test Title",
              "category": "Fashion",
              "aspectRatio": "9:16",
              "elements": [
                {
                  "type": "background",
                  "id": "bg",
                  "color": "#C91F2C"
                },
                {
                  "type": "text",
                  "id": "heading",
                  "defaultText": "NEW",
                  "x": 0.1,
                  "y": 0.1,
                  "width": 0.8,
                  "height": 0.2
                }
              ]
            }
        """.trimIndent()

        val result = TemplateParser.parseJson(json)
        assertTrue(result.isSuccess)

        val manifest = result.getOrNull()
        assertNotNull(manifest)
        assertEquals("test_001", manifest!!.id)
        assertEquals("Test Title", manifest.title)
        assertEquals("Fashion", manifest.category)
        assertEquals(2, manifest.elements.size)

        val bg = manifest.elements[0] as TemplateElement.Background
        assertEquals("#C91F2C", bg.color)

        val text = manifest.elements[1] as TemplateElement.Text
        assertEquals("NEW", text.defaultText)
    }

    @Test
    fun validateManifest_detectsBlankIdAndEmptyElements() {
        val json = """
            {
              "schemaVersion": 1,
              "id": "",
              "title": "Invalid Template",
              "category": "Minimal",
              "elements": []
            }
        """.trimIndent()

        val manifest = TemplateParser.parseJson(json).getOrThrow()
        val errors = TemplateValidator.validate(manifest)

        assertTrue(errors.isNotEmpty())
        assertTrue(errors.any { it.contains("Template ID cannot be blank") })
        assertTrue(errors.any { it.contains("Template must contain at least one element") })
    }

    @Test
    fun numericAspectRatio_computesCorrectFraction() {
        val json = """
            {
              "schemaVersion": 1,
              "id": "aspect_test",
              "title": "Aspect Test",
              "category": "Stories",
              "aspectRatio": "9:16",
              "elements": [
                { "type": "background", "id": "bg" }
              ]
            }
        """.trimIndent()

        val manifest = TemplateParser.parseJson(json).getOrThrow()
        assertEquals(9f / 16f, manifest.numericAspectRatio, 0.001f)
    }

    @Test
    fun parseTravelCollageTemplate_validatesSuccessfully() {
        val json = javaClass.classLoader?.getResourceAsStream("templates/travel_collage_005.json")
            ?.bufferedReader()?.use { it.readText() }

        if (json != null) {
            val parseResult = TemplateParser.parseJson(json)
            assertTrue(parseResult.isSuccess)
            val manifest = parseResult.getOrThrow()
            assertEquals("travel_collage_005", manifest.id)
            assertEquals("My Travels Collage", manifest.title)
            val errors = TemplateValidator.validate(manifest)
            assertTrue(errors.isEmpty())
        }
    }
}
