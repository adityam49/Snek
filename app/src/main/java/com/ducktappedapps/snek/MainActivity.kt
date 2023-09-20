package com.ducktappedapps.snek

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import com.ducktappedapps.snek.CurrentScreen.GAME_OVER_SCREEN
import com.ducktappedapps.snek.CurrentScreen.GAME_SCREEN
import com.ducktappedapps.snek.CurrentScreen.START_SCREEN
import com.ducktappedapps.snek.GameOverScreenAction.Exit
import com.ducktappedapps.snek.GameOverScreenAction.ReplaySnek
import com.ducktappedapps.snek.GameScreenAction.GameOver
import com.ducktappedapps.snek.GameScreenAction.MoveDown
import com.ducktappedapps.snek.GameScreenAction.MoveLeft
import com.ducktappedapps.snek.GameScreenAction.MoveRight
import com.ducktappedapps.snek.GameScreenAction.MoveUp
import com.ducktappedapps.snek.GameScreenAction.PauseGame
import com.ducktappedapps.snek.GameScreenAction.ResumeGame
import com.ducktappedapps.snek.StartScreenAction.ExitGame
import com.ducktappedapps.snek.StartScreenAction.StartGame
import com.ducktappedapps.snek.ui.theme.SnekTheme

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels<GameViewModelImpl>()
    private val interactor: GameInteractor by viewModels<GameViewModelImpl>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SnekTheme {
                OnLifecycleEvent { _, event ->
                    if (event == Lifecycle.Event.ON_PAUSE) {
                        interactor.pauseGame()
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (viewModel.currentScreen.collectAsState().value) {
                        START_SCREEN -> StartScreen(
                            modifier = Modifier.fillMaxSize(),
                            performAction = {
                                when (it) {
                                    ExitGame -> {
                                        interactor.exitApp()
                                        finish()
                                    }

                                    StartGame -> interactor.startGame()
                                }
                            }
                        )

                        GAME_SCREEN -> {
                            if (viewModel.isGamePause.collectAsState().value) {
                                GamePauseDialog(viewModel = viewModel,interactor = interactor)
                            }
                            GameScreen(
                                modifier = Modifier.fillMaxSize(),
                                gameState = viewModel.gameState,
                                highScore = viewModel.highScore,
                            ) {
                                when (it) {
                                    MoveDown -> interactor.moveDown()
                                    MoveLeft -> interactor.moveLeft()
                                    MoveRight -> interactor.moveRight()
                                    MoveUp -> interactor.moveUp()
                                    PauseGame -> interactor.pauseGame()
                                    GameOver -> interactor.showGameOverScreen()
                                    ResumeGame -> interactor.resumeGame()
                                }
                            }
                        }

                        GAME_OVER_SCREEN -> GameOverScreen(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            when (it) {
                                Exit -> interactor.exitGame()
                                ReplaySnek -> interactor.startGame()
                            }
                        }
                    }
                }
            }
        }
    }
}


