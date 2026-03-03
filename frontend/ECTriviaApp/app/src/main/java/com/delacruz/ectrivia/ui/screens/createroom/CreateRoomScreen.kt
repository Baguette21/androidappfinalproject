package com.ectrvia.ectrivia.ui.screens.createroom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ectrvia.ectrivia.ui.components.ECTriviaButton
import com.ectrvia.ectrivia.ui.components.ECTriviaTextField
import com.ectrvia.ectrivia.ui.components.ErrorDialog
import com.ectrvia.ectrivia.ui.components.LoadingIndicator
import com.ectrvia.ectrivia.ui.theme.*
import com.ectrvia.ectrivia.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomScreen(
    onNavigateBack: () -> Unit,
    onThemeSelected: (String, Int) -> Unit,
    onCustomQuestions: (String, Long, Boolean) -> Unit,
    onRoomCreated: (String, Long, Boolean) -> Unit,
    viewModel: CreateRoomViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.roomCreated) {
        uiState.createdRoomCode?.let { roomCode ->
            uiState.playerId?.let { playerId ->
                if (uiState.isThemeBased) {
                    onRoomCreated(roomCode, playerId, true)
                } else {
                    onCustomQuestions(roomCode, playerId, true)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Room", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ECTriviaBackground
                )
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
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Room Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Host nickname
                    ECTriviaTextField(
                        value = uiState.hostNickname,
                        onValueChange = { viewModel.updateHostNickname(it) },
                        label = "Your Nickname"
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Theme mode is enabled by default",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Timer Slider
                    Text(
                        text = "Question Timer: ${uiState.timerSeconds}s",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                    Slider(
                        value = uiState.timerSeconds.toFloat(),
                        onValueChange = { viewModel.updateTimer(it.toInt()) },
                        valueRange = Constants.MIN_TIMER_SECONDS.toFloat()..Constants.MAX_TIMER_SECONDS.toFloat(),
                        steps = Constants.MAX_TIMER_SECONDS - Constants.MIN_TIMER_SECONDS - 1,
                        colors = SliderDefaults.colors(
                            thumbColor = ECTriviaPrimary,
                            activeTrackColor = ECTriviaPrimary
                        )
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    ECTriviaButton(
                        text = "Select Theme",
                        onClick = {
                            onThemeSelected(uiState.hostNickname, uiState.timerSeconds)
                        },
                        enabled = uiState.hostNickname.isNotBlank()
                    )
                }
            }

            uiState.error?.let { error ->
                ErrorDialog(
                    title = "Error",
                    message = error,
                    onDismiss = { viewModel.clearError() }
                )
            }
        }
    }
}
