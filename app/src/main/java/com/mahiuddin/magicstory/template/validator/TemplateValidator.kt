package com.mahiuddin.magicstory.template.validator

import com.mahiuddin.magicstory.domain.model.TemplateManifest

object TemplateValidator {
    const val MAX_SUPPORTED_SCHEMA_VERSION = 1

    fun validate(manifest: TemplateManifest): List<String> {
        val errors = mutableListOf<String>()

        if (manifest.schemaVersion > MAX_SUPPORTED_SCHEMA_VERSION) {
            errors.add("Unsupported schema version: ${manifest.schemaVersion}. Max supported is $MAX_SUPPORTED_SCHEMA_VERSION.")
        }
        if (manifest.id.isBlank()) {
            errors.add("Template ID cannot be blank.")
        }
        if (manifest.title.isBlank()) {
            errors.add("Template title cannot be blank.")
        }
        if (manifest.category.isBlank()) {
            errors.add("Template category cannot be blank.")
        }
        if (manifest.elements.isEmpty()) {
            errors.add("Template must contain at least one element.")
        }

        val seenIds = mutableSetOf<String>()
        manifest.elements.forEachIndexed { index, element ->
            if (element.id.isBlank()) {
                errors.add("Element at index $index has blank ID.")
            } else if (!seenIds.add(element.id)) {
                errors.add("Duplicate element ID found: '${element.id}'.")
            }
        }

        return errors
    }
}
