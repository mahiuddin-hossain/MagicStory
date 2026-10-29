package com.mahiuddin.magicstory.domain.repository

import com.mahiuddin.magicstory.domain.model.TemplateManifest
import kotlinx.coroutines.flow.Flow

interface TemplateRepository {
    fun getTemplates(): Flow<List<TemplateManifest>>
    suspend fun getTemplateById(id: String): TemplateManifest?
    fun getCategories(): Flow<List<String>>
}
