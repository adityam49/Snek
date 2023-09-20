package com.ducktappedapps.snek

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

interface GameViewModel {
    val currentScreen: StateFlow<CurrentScreen>
    val toastMessageEvent: SharedFlow<String>
    val isGamePause: StateFlow<Boolean>
    val gameState: GameState
    val highScore: StateFlow<Int>
}

interface GameInteractor {
    fun startGame()
    fun exitGame()
    fun showGameOverScreen()
    fun exitApp()

    fun moveLeft()
    fun moveRight()
    fun moveUp()
    fun moveDown()
    fun pauseGame()
    fun resumeGame()
}

class GameViewModelImpl(
    private val application: Application
) : AndroidViewModel(application), GameViewModel, GameInteractor {
    override val currentScreen: MutableStateFlow<CurrentScreen> =
        MutableStateFlow(CurrentScreen.START_SCREEN)
    override val toastMessageEvent: MutableSharedFlow<String> = MutableSharedFlow()
    override val isGamePause: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override var gameState: GameState = GameState()
    override val highScore: MutableStateFlow<Int> = MutableStateFlow(0)

    private var gameLoop: Job? = null

    override fun startGame() {
        currentScreen.value = CurrentScreen.GAME_SCREEN
        gameState = GameState()
        highScore.value = application
            .getSharedPreferences(
                application.packageName,
                Context.MODE_PRIVATE
            )
            .getInt("High Score", 0)

        resumeGame()
    }


    override fun exitGame() {
        saveHighScore()
        currentScreen.value = CurrentScreen.START_SCREEN
    }

    override fun showGameOverScreen() {
        saveHighScore()
        currentScreen.value = CurrentScreen.GAME_OVER_SCREEN
    }

    override fun exitApp() {
        viewModelScope.launch {
            toastMessageEvent.emit("Exiting...")
        }
    }

    override fun moveLeft() {
        gameState.moveLeft()
    }

    override fun moveRight() {
        gameState.moveRight()
    }

    override fun moveUp() {
        gameState.moveUp()
    }

    override fun moveDown() {
        gameState.moveDown()
    }

    override fun pauseGame() {
        gameLoop?.cancel()
        gameLoop = null
        isGamePause.value = true
    }

    override fun resumeGame() {
        if (gameLoop == null) {
            isGamePause.value = false
            gameLoop = viewModelScope.launch {
                while (true) {
                    delay(lerp(300f, 10f, (gameState.snekSpeed.value)).toLong())
                    gameState.updateStateOnLoopTick()
                }
            }
        }
    }

    private fun saveHighScore() {
        viewModelScope.launch {
            gameState.score.collectLatest { latestScore ->
                if (highScore.value < latestScore) {
                    application
                        .getSharedPreferences(application.packageName, Context.MODE_PRIVATE)
                        .edit { putInt("High Score", latestScore) }
                }
            }
        }
    }
}