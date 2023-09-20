package com.ducktappedapps.snek

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog


@Composable
fun GamePauseDialog(
    viewModel : GameViewModel,
    interactor : GameInteractor,
) {
    Dialog(onDismissRequest = {
        interactor.resumeGame()
    }) {
        Card {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    modifier = Modifier.padding(16.dp),
                    onClick = { interactor.resumeGame() }) {
                    Text(text = "Resume Game")
                }
                Button(
                    modifier = Modifier.padding(16.dp),
                    onClick = { interactor.exitGame() }) {
                    Text(text = "Exit game")
                }
                Text(
                    text = "snek speed ${
                        ((viewModel.gameState.snekSpeed.collectAsState(
                            0.5f
                        ).value) * 100).toInt()
                    }"
                )
                Slider(
                    value = (viewModel.gameState.snekSpeed.collectAsState(
                        0.5f
                    ).value),
                    onValueChange = {
                        viewModel.gameState.snekSpeed.value = it
                    },
                    steps = 100
                )
            }
        }
    }
}