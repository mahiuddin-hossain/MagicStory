package com.mahiuddin.magicstory.template.parser

import com.mahiuddin.magicstory.data.model.TemplateManifestDto
import com.mahiuddin.magicstory.domain.model.TemplateManifest
import kotlinx.serialization.json.Json

object TemplateParser {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    fun parseJson(jsonString: String): Result<TemplateManifest> {
        return runCatching {
            val dto = json.decodeFromString<TemplateManifestDto>(jsonString)
            dto.toDomain()
        }
    }
}
