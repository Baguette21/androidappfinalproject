package com.ectrvia.ectrivia.ui.screens.createroom

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ectrvia.ectrivia.ui.components.ECTriviaButton
import com.ectrvia.ectrivia.ui.components.ECTriviaTextField
import com.ectrvia.ectrivia.ui.components.ErrorDialog
import com.ectrvia.ectrivia.ui.components.LoadingIndicator
import com.ectrvia.ectrivia.ui.theme.AnswerBlue
import com.ectrvia.ectrivia.ui.theme.AnswerGreen
import com.ectrvia.ectrivia.ui.theme.AnswerRed
import com.ectrvia.ectrivia.ui.theme.AnswerYellow
import com.ectrvia.ectrivia.ui.theme.ECTriviaBackground
import com.ectrvia.ectrivia.ui.theme.ECTriviaPrimary
import com.ectrvia.ectrivia.ui.theme.ECTriviaSurfaceVariant
import com.ectrvia.ectrivia.ui.theme.IncorrectRed
import com.ectrvia.ectrivia.ui.theme.TextPrimary
import com.ectrvia.ectrivia.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagerScreen(
    onNavigateBack: () -> Unit,
    viewModel: CategoryManagerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Categories", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ECTriviaBackground)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ECTriviaBackground)
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                LoadingIndicator()
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    CategoryCreateCard(
                        name = uiState.newCategoryName,
                        description = uiState.newCategoryDescription,
                        onNameChange = viewModel::updateCategoryName,
                        onDescriptionChange = viewModel::updateCategoryDescription,
                        onCreate = viewModel::createCategory
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Categories",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        IconButton(
                            onClick = viewModel::openDeleteCategoryDialog,
                            enabled = uiState.selectedCategoryId != null
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete selected category",
                                tint = IncorrectRed
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.categories.forEach { category ->
                            ElevatedFilterChip(
                                selected = uiState.selectedCategoryId == category.id,
                                onClick = { viewModel.selectCategory(category.id) },
                                label = { Text("${category.name} (${category.questionCount})") }
                            )
                        }
                    }
                }

                item {
                    CategoryQuestionCreateCard(
                        questionText = uiState.newQuestionText,
                        answers = uiState.newAnswers,
                        correctAnswerIndex = uiState.newCorrectAnswerIndex,
                        onQuestionTextChange = viewModel::updateQuestionText,
                        onAnswerChange = viewModel::updateAnswer,
                        onCorrectSelect = viewModel::setCorrectAnswer,
                        onAddQuestion = viewModel::addQuestionToSelectedCategory,
                        enabled = uiState.selectedCategoryId != null
                    )
                }

                item {
                    Text(
                        text = "Category Questions",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }

                if (uiState.questions.isEmpty()) {
                    item {
                        Text(
                            text = "No questions in selected category.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                } else {
                    items(uiState.questions) { question ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ECTriviaSurfaceVariant)
                                .clickable { viewModel.startEditingQuestion(question) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = question.questionText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.deleteQuestion(question.id) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete question",
                                    tint = IncorrectRed
                                )
                            }
                        }
                    }
                }
            }

            uiState.error?.let { error ->
                ErrorDialog(
                    title = "Error",
                    message = error,
                    onDismiss = viewModel::clearError
                )
            }

            if (uiState.isEditDialogOpen) {
                EditQuestionDialog(
                    questionText = uiState.editQuestionText,
                    answers = uiState.editAnswers,
                    correctAnswerIndex = uiState.editCorrectAnswerIndex,
                    onQuestionTextChange = viewModel::updateEditQuestionText,
                    onAnswerChange = viewModel::updateEditAnswer,
                    onCorrectSelect = viewModel::setEditCorrectAnswer,
                    onDismiss = viewModel::closeEditDialog,
                    onSave = viewModel::saveEditedQuestion
                )
            }

            if (uiState.isDeleteCategoryDialogOpen) {
                AlertDialog(
                    onDismissRequest = viewModel::closeDeleteCategoryDialog,
                    title = { Text("Delete Category") },
                    text = { Text("This will remove the selected category from theme options. Continue?") },
                    confirmButton = {
                        TextButton(onClick = viewModel::deleteSelectedCategory) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = viewModel::closeDeleteCategoryDialog) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun EditQuestionDialog(
    questionText: String,
    answers: List<String>,
    correctAnswerIndex: Int,
    onQuestionTextChange: (String) -> Unit,
    onAnswerChange: (Int, String) -> Unit,
    onCorrectSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val answerColors = listOf(AnswerRed, AnswerBlue, AnswerYellow, AnswerGreen)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Question", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ECTriviaTextField(
                    value = questionText,
                    onValueChange = onQuestionTextChange,
                    label = "Question"
                )

                answers.forEachIndexed { index, answer ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(answerColors[index])
                                .clickable { onCorrectSelect(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (correctAnswerIndex == index) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Correct",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Text(text = ('A' + index).toString(), color = TextPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = answer,
                            onValueChange = { onAnswerChange(index, it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Answer ${index + 1}") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun CategoryCreateCard(
    name: String,
    description: String,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCreate: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ECTriviaSurfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Create Category", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            ECTriviaTextField(value = name, onValueChange = onNameChange, label = "Category Name")
            ECTriviaTextField(value = description, onValueChange = onDescriptionChange, label = "Description")
            ECTriviaButton(text = "Create Category", onClick = onCreate, enabled = name.isNotBlank())
        }
    }
}

@Composable
private fun CategoryQuestionCreateCard(
    questionText: String,
    answers: List<String>,
    correctAnswerIndex: Int,
    onQuestionTextChange: (String) -> Unit,
    onAnswerChange: (Int, String) -> Unit,
    onCorrectSelect: (Int) -> Unit,
    onAddQuestion: () -> Unit,
    enabled: Boolean
) {
    val answerColors = listOf(AnswerRed, AnswerBlue, AnswerYellow, AnswerGreen)

    Card(
        colors = CardDefaults.cardColors(containerColor = ECTriviaSurfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Add Question to Category", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            ECTriviaTextField(
                value = questionText,
                onValueChange = onQuestionTextChange,
                label = "Question"
            )

            answers.forEachIndexed { index, answer ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(answerColors[index])
                            .clickable { onCorrectSelect(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (correctAnswerIndex == index) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Correct",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(text = ('A' + index).toString(), color = TextPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = answer,
                        onValueChange = { onAnswerChange(index, it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Answer ${index + 1}") }
                    )
                }
            }

            ECTriviaButton(
                text = "Add Question",
                onClick = onAddQuestion,
                enabled = enabled
            )
        }
    }
}
