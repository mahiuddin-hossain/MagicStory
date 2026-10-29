package com.mahiuddin.magicstory.ui.editor

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahiuddin.magicstory.domain.model.EditorSnapshot
import com.mahiuddin.magicstory.domain.model.ExportResolution
import com.mahiuddin.magicstory.domain.model.MagicStoryEditorState
import com.mahiuddin.magicstory.domain.repository.TemplateRepository
import com.mahiuddin.magicstory.template.export.ImageExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditorViewModel(
    private val repository: TemplateRepository
) : ViewModel() {

    private val _state = MutableStateFlow<MagicStoryEditorState?>(null)
    val state: StateFlow<MagicStoryEditorState?> = _state.asStateFlow()

    fun loadTemplate(templateId: String) {
        viewModelScope.launch {
            val manifest = repository.getTemplateById(templateId)
            if (manifest != null) {
                val initialSnapshot = EditorSnapshot(emptyMap(), emptyMap(), emptyMap(), emptyMap())
                _state.value = MagicStoryEditorState(
                    template = manifest,
                    history = listOf(initialSnapshot),
                    historyIndex = 0
                )
            }
        }
    }

    fun selectElement(elementId: String?) {
        _state.value = _state.value?.copy(selectedElementId = elementId)
    }

    fun updateText(elementId: String, newText: String) {
        val currentState = _state.value ?: return
        val newTexts = currentState.editedTexts.toMutableMap().apply {
            put(elementId, newText)
        }

        pushState(currentState, newTexts, currentState.photoUris, currentState.elementPositions, currentState.textFontSizes)
    }

    fun updatePhoto(slotId: String, uri: Uri) {
        val currentState = _state.value ?: return
        val newPhotos = currentState.photoUris.toMutableMap().apply {
            put(slotId, uri.toString())
        }

        pushState(currentState, currentState.editedTexts, newPhotos, currentState.elementPositions, currentState.textFontSizes)
    }

    fun updateElementPosition(elementId: String, newX: Float, newY: Float) {
        val currentState = _state.value ?: return
        val newPositions = currentState.elementPositions.toMutableMap().apply {
            put(elementId, Pair(newX, newY))
        }

        _state.value = currentState.copy(
            elementPositions = newPositions,
            selectedElementId = elementId
        )
    }

    fun updateFontSize(elementId: String, newSizeSp: Float) {
        val currentState = _state.value ?: return
        val newSizes = currentState.textFontSizes.toMutableMap().apply {
            put(elementId, newSizeSp)
        }

        pushState(currentState, currentState.editedTexts, currentState.photoUris, currentState.elementPositions, newSizes)
    }

    fun commitDragEnd() {
        val currentState = _state.value ?: return
        pushState(currentState, currentState.editedTexts, currentState.photoUris, currentState.elementPositions, currentState.textFontSizes)
    }

    fun setExportResolution(resolution: ExportResolution) {
        _state.value = _state.value?.copy(exportResolution = resolution)
    }

    private fun pushState(
        currentState: MagicStoryEditorState,
        newTexts: Map<String, String>,
        newPhotos: Map<String, String>,
        newPositions: Map<String, Pair<Float, Float>>,
        newSizes: Map<String, Float>
    ) {
        val newSnapshot = EditorSnapshot(newTexts, newPhotos, newPositions, newSizes)
        val trimmedHistory = currentState.history.take(currentState.historyIndex + 1) + newSnapshot

        _state.value = currentState.copy(
            editedTexts = newTexts,
            photoUris = newPhotos,
            elementPositions = newPositions,
            textFontSizes = newSizes,
            history = trimmedHistory,
            historyIndex = trimmedHistory.size - 1
        )
    }

    fun undo() {
        val currentState = _state.value ?: return
        if (currentState.canUndo) {
            val prevIndex = currentState.historyIndex - 1
            val prevSnapshot = currentState.history[prevIndex]
            _state.value = currentState.copy(
                editedTexts = prevSnapshot.editedTexts,
                photoUris = prevSnapshot.photoUris,
                elementPositions = prevSnapshot.elementPositions,
                textFontSizes = prevSnapshot.textFontSizes,
                historyIndex = prevIndex
            )
        }
    }

    fun redo() {
        val currentState = _state.value ?: return
        if (currentState.canRedo) {
            val nextIndex = currentState.historyIndex + 1
            val nextSnapshot = currentState.history[nextIndex]
            _state.value = currentState.copy(
                editedTexts = nextSnapshot.editedTexts,
                photoUris = nextSnapshot.photoUris,
                elementPositions = nextSnapshot.elementPositions,
                textFontSizes = nextSnapshot.textFontSizes,
                historyIndex = nextIndex
            )
        }
    }

    fun resetToDefaults() {
        val currentState = _state.value ?: return
        val defaultSnapshot = EditorSnapshot(emptyMap(), emptyMap(), emptyMap(), emptyMap())
        _state.value = currentState.copy(
            editedTexts = emptyMap(),
            photoUris = emptyMap(),
            elementPositions = emptyMap(),
            textFontSizes = emptyMap(),
            selectedElementId = null,
            history = listOf(defaultSnapshot),
            historyIndex = 0
        )
    }

    fun exportImage(context: Context) {
        val currentState = _state.value ?: return
        _state.value = currentState.copy(isExporting = true, errorMessage = null)

        viewModelScope.launch {
            val result = ImageExporter.exportImage(
                context = context,
                manifest = currentState.template,
                editorState = currentState
            )

            result.fold(
                onSuccess = { uri ->
                    _state.value = _state.value?.copy(
                        isExporting = false,
                        exportResultUri = uri.toString()
                    )
                },
                onFailure = { error ->
                    _state.value = _state.value?.copy(
                        isExporting = false,
                        errorMessage = error.localizedMessage ?: "Failed to export image."
                    )
                }
            )
        }
    }

    fun clearExportResult() {
        _state.value = _state.value?.copy(exportResultUri = null, errorMessage = null)
    }
}
