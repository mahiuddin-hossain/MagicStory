package com.mahiuddin.magicstory.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahiuddin.magicstory.domain.model.TemplateManifest
import com.mahiuddin.magicstory.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val repository: TemplateRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val categories: StateFlow<List<String>> = repository.getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All", "Fashion", "Stories", "Minimal", "Promotional"))

    val templates: StateFlow<List<TemplateManifest>> = combine(
        repository.getTemplates(),
        _selectedCategory,
        _searchQuery
    ) { allTemplates, category, query ->
        allTemplates.filter { template ->
            val matchesCategory = category == "All" || template.category.equals(category, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                    template.title.contains(query, ignoreCase = true) ||
                    template.category.contains(query, ignoreCase = true) ||
                    template.description.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}
