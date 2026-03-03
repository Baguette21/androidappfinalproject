package com.ectrvia.ectrivia.ui.screens.createroom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ectrvia.ectrivia.data.model.Category
import com.ectrvia.ectrivia.data.model.Question
import com.ectrvia.ectrivia.data.repository.QuestionInput
import com.ectrvia.ectrivia.data.repository.QuestionRepository
import com.ectrvia.ectrivia.util.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryManagerViewModel @Inject constructor(
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryManagerUiState())
    val uiState: StateFlow<CategoryManagerUiState> = _uiState.asStateFlow()

    fun initialize() {
        if (_uiState.value.categories.isEmpty()) {
            loadCategories()
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = questionRepository.getCategories()) {
                is NetworkResult.Success -> {
                    val selectedId = _uiState.value.selectedCategoryId
                    val categories = result.data
                    val nextSelected = when {
                        categories.isEmpty() -> null
                        selectedId != null && categories.any { it.id == selectedId } -> selectedId
                        else -> categories.first().id
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            categories = categories,
                            selectedCategoryId = nextSelected
                        )
                    }
                    if (nextSelected != null) {
                        loadCategoryQuestions(nextSelected)
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun selectCategory(categoryId: Long) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
        loadCategoryQuestions(categoryId)
    }

    fun openDeleteCategoryDialog() {
        if (_uiState.value.selectedCategoryId == null) {
            _uiState.update { it.copy(error = "Select a category first") }
            return
        }
        _uiState.update { it.copy(isDeleteCategoryDialogOpen = true) }
    }

    fun closeDeleteCategoryDialog() {
        _uiState.update { it.copy(isDeleteCategoryDialogOpen = false) }
    }

    fun deleteSelectedCategory() {
        val categoryId = _uiState.value.selectedCategoryId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isDeleteCategoryDialogOpen = false) }
            when (val result = questionRepository.deleteCategory(categoryId)) {
                is NetworkResult.Success -> {
                    val updatedCategories = _uiState.value.categories.filterNot { it.id == categoryId }
                    val nextSelected = updatedCategories.firstOrNull()?.id
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            categories = updatedCategories,
                            selectedCategoryId = nextSelected,
                            questions = if (nextSelected == null) emptyList() else it.questions
                        )
                    }
                    if (nextSelected != null) {
                        loadCategoryQuestions(nextSelected)
                    } else {
                        _uiState.update { it.copy(questions = emptyList()) }
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun updateCategoryName(value: String) {
        _uiState.update { it.copy(newCategoryName = value) }
    }

    fun updateCategoryDescription(value: String) {
        _uiState.update { it.copy(newCategoryDescription = value) }
    }

    fun createCategory() {
        val name = _uiState.value.newCategoryName.trim()
        val description = _uiState.value.newCategoryDescription.trim()

        if (name.isBlank()) {
            _uiState.update { it.copy(error = "Category name is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = questionRepository.createCategory(name, description)) {
                is NetworkResult.Success -> {
                    val created = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            newCategoryName = "",
                            newCategoryDescription = "",
                            categories = it.categories + created,
                            selectedCategoryId = created.id
                        )
                    }
                    loadCategoryQuestions(created.id)
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun updateQuestionText(value: String) {
        _uiState.update { it.copy(newQuestionText = value) }
    }

    fun updateAnswer(index: Int, value: String) {
        val answers = _uiState.value.newAnswers.toMutableList()
        answers[index] = value
        _uiState.update { it.copy(newAnswers = answers) }
    }

    fun setCorrectAnswer(index: Int) {
        _uiState.update { it.copy(newCorrectAnswerIndex = index) }
    }

    fun addQuestionToSelectedCategory() {
        val categoryId = _uiState.value.selectedCategoryId
        if (categoryId == null) {
            _uiState.update { it.copy(error = "Select a category first") }
            return
        }

        val questionText = _uiState.value.newQuestionText.trim()
        val answers = _uiState.value.newAnswers.map { it.trim() }
        val correct = _uiState.value.newCorrectAnswerIndex

        if (questionText.isBlank() || answers.any { it.isBlank() }) {
            _uiState.update { it.copy(error = "Question and all answer options are required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (
                val result = questionRepository.addQuestionToCategory(
                    categoryId,
                    QuestionInput(
                        questionText = questionText,
                        answers = answers,
                        correctAnswerIndex = correct,
                        timerSeconds = 15
                    )
                )
            ) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            newQuestionText = "",
                            newAnswers = listOf("", "", "", ""),
                            newCorrectAnswerIndex = 0
                        )
                    }
                    loadCategoryQuestions(categoryId)
                    loadCategories()
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun deleteQuestion(questionId: Long) {
        val categoryId = _uiState.value.selectedCategoryId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = questionRepository.deleteCategoryQuestion(categoryId, questionId)) {
                is NetworkResult.Success -> {
                    loadCategoryQuestions(categoryId)
                    loadCategories()
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun startEditingQuestion(question: Question) {
        val answerTexts = question.answers.sortedBy { it.index }.map { it.text }.toMutableList()
        while (answerTexts.size < 4) {
            answerTexts.add("")
        }
        _uiState.update {
            it.copy(
                editingQuestionId = question.id,
                editQuestionText = question.questionText,
                editAnswers = answerTexts.take(4),
                editCorrectAnswerIndex = question.correctAnswerIndex,
                isEditDialogOpen = true
            )
        }
    }

    fun closeEditDialog() {
        _uiState.update {
            it.copy(
                isLoading = false,
                isEditDialogOpen = false,
                editingQuestionId = null,
                editQuestionText = "",
                editAnswers = listOf("", "", "", ""),
                editCorrectAnswerIndex = 0
            )
        }
    }

    fun updateEditQuestionText(value: String) {
        _uiState.update { it.copy(editQuestionText = value) }
    }

    fun updateEditAnswer(index: Int, value: String) {
        val updated = _uiState.value.editAnswers.toMutableList()
        updated[index] = value
        _uiState.update { it.copy(editAnswers = updated) }
    }

    fun setEditCorrectAnswer(index: Int) {
        _uiState.update { it.copy(editCorrectAnswerIndex = index) }
    }

    fun saveEditedQuestion() {
        val categoryId = _uiState.value.selectedCategoryId
        val questionId = _uiState.value.editingQuestionId
        if (categoryId == null || questionId == null) {
            _uiState.update { it.copy(error = "Category or question is missing") }
            return
        }

        val questionText = _uiState.value.editQuestionText.trim()
        val answers = _uiState.value.editAnswers.map { it.trim() }
        val correctIndex = _uiState.value.editCorrectAnswerIndex

        if (questionText.isBlank() || answers.any { it.isBlank() }) {
            _uiState.update { it.copy(error = "Question and all answer options are required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (
                val result = questionRepository.updateCategoryQuestion(
                    categoryId = categoryId,
                    questionId = questionId,
                    question = QuestionInput(
                        questionText = questionText,
                        answers = answers,
                        correctAnswerIndex = correctIndex,
                        timerSeconds = 15
                    )
                )
            ) {
                is NetworkResult.Success -> {
                    closeEditDialog()
                    loadCategoryQuestions(categoryId)
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    private fun loadCategoryQuestions(categoryId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = questionRepository.getAllCategoryQuestions(categoryId)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, questions = result.data) }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class CategoryManagerUiState(
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val questions: List<Question> = emptyList(),
    val newCategoryName: String = "",
    val newCategoryDescription: String = "",
    val newQuestionText: String = "",
    val newAnswers: List<String> = listOf("", "", "", ""),
    val newCorrectAnswerIndex: Int = 0,
    val isDeleteCategoryDialogOpen: Boolean = false,
    val isEditDialogOpen: Boolean = false,
    val editingQuestionId: Long? = null,
    val editQuestionText: String = "",
    val editAnswers: List<String> = listOf("", "", "", ""),
    val editCorrectAnswerIndex: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)
