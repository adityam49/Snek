package com.ducktappedapps.snek

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ducktappedapps.snek.GameOverScreenAction.*

sealed class GameOverScreenAction {
    object ReplaySnek : GameOverScreenAction()
    object Exit : GameOverScreenAction()
}

@Composable
fun GameOverScreen(
    modifier: Modifier,
    performGameOverScreenAction: (GameOverScreenAction) -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = { performGameOverScreenAction(ReplaySnek) }) {
            Text(text = "Replay Snek")
        }
        Button(onClick = { performGameOverScreenAction(Exit) }) {
            Text(text = "Exit")
        }
    }
}