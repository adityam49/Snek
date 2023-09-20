package com.ducktappedapps.snek

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

sealed class StartScreenAction {
    object StartGame : StartScreenAction()
    object ExitGame : StartScreenAction()
}

@Composable
fun StartScreen(
    modifier: Modifier,
    performAction: (StartScreenAction) -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = { performAction(StartScreenAction.StartGame) }) {
            Text(text = "Play Snek")
        }
        Button(onClick = { performAction(StartScreenAction.ExitGame) }) {
            Text(text = "Exit")
        }
    }
}