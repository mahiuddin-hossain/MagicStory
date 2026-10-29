package com.mahiuddin.magicstory.data.local

import android.content.Context
import com.mahiuddin.magicstory.domain.model.TemplateManifest
import com.mahiuddin.magicstory.template.parser.TemplateParser
import com.mahiuddin.magicstory.template.validator.TemplateValidator

class AssetTemplateDataSource(private val context: Context) {

    fun loadTemplatesFromAssets(): List<TemplateManifest> {
        val templates = mutableListOf<TemplateManifest>()

        return try {
            val assetFiles = context.assets.list("templates") ?: emptyArray()

            assetFiles.filter { it.endsWith(".json") }.forEach { fileName ->
                try {
                    val jsonString = context.assets.open("templates/$fileName").use { inputStream ->
                        inputStream.bufferedReader().use { it.readText() }
                    }

                    val parseResult = TemplateParser.parseJson(jsonString)
                    parseResult.getOrNull()?.let { manifest ->
                        val validationErrors = TemplateValidator.validate(manifest)
                        if (validationErrors.isEmpty()) {
                            templates.add(manifest)
                        } else {
                            System.err.println("Skipping invalid template '$fileName': $validationErrors")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            templates
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
