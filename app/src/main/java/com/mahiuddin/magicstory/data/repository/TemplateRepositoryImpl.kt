package com.mahiuddin.magicstory.data.repository

import com.mahiuddin.magicstory.data.local.AssetTemplateDataSource
import com.mahiuddin.magicstory.domain.model.TemplateManifest
import com.mahiuddin.magicstory.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class TemplateRepositoryImpl(
    private val dataSource: AssetTemplateDataSource
) : TemplateRepository {

    private val _templates = MutableStateFlow<List<TemplateManifest>>(emptyList())
    val templates = _templates.asStateFlow()

    init {
        refreshTemplates()
    }

    fun refreshTemplates() {
        val loaded = dataSource.loadTemplatesFromAssets()
        _templates.value = loaded
    }

    override fun getTemplates(): Flow<List<TemplateManifest>> = templates

    override suspend fun getTemplateById(id: String): TemplateManifest? {
        return _templates.value.find { it.id == id }
    }

    override fun getCategories(): Flow<List<String>> {
        return templates.map { list ->
            val categories = mutableListOf("All")
            val distinctCategories = list.map { it.category }.distinct().sorted()
            categories.addAll(distinctCategories)
            categories
        }
    }
}
