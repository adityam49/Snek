package com.ducktappedapps.snek

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ducktappedapps.snek.GameState.GameEvent.SnekBitItself
import com.ducktappedapps.snek.GameState.GameEvent.SnekHitAWall
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest

sealed class GameScreenAction {
    object MoveUp : GameScreenAction()
    object MoveDown : GameScreenAction()
    object MoveLeft : GameScreenAction()
    object MoveRight : GameScreenAction()
    object PauseGame : GameScreenAction()
    object ResumeGame : GameScreenAction()
    object GameOver : GameScreenAction()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    modifier: Modifier,
    gameState: GameState,
    highScore : StateFlow<Int>,
    performGameScreenAction: (GameScreenAction) -> Unit,
) {
    BackHandler {
        performGameScreenAction(GameScreenAction.PauseGame)
    }
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FilledIconButton(
                onClick = { performGameScreenAction(GameScreenAction.PauseGame) },
            ) {
                Icon(Icons.Default.Settings, Icons.Default.Settings.name)
            }
        },
        topBar = {
            TopAppBar(title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.wrapContentSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Score", fontSize = 16.sp)
                        ScoreCounter(
                            modifier = Modifier
                                .wrapContentSize()
                                .padding(8.dp),
                            value = gameState.score.collectAsState(initial = 0).value
                        )
                    }

                    Row(
                        modifier = Modifier.wrapContentSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "High Score", fontSize = 16.sp)
                        ScoreCounter(
                            modifier = Modifier
                                .wrapContentSize()
                                .padding(8.dp),
                            value = highScore.collectAsState(initial = 0).value
                        )
                    }
                }
            })
        }
    ) { scaffoldPadding ->
        val snekBody = gameState.snekSparseArray.collectAsState().value
        val foodItems = gameState.foodSparseArray.collectAsState().value
        LaunchedEffect(key1 = Unit) {
            gameState.gameEvent.collectLatest {
                when (it) {
                    SnekBitItself -> performGameScreenAction(GameScreenAction.GameOver)
                    SnekHitAWall -> performGameScreenAction(GameScreenAction.GameOver)
                }
            }
        }
        GameCanvas(
            modifier = modifier.padding(scaffoldPadding),
            snekBody = snekBody,
            foodItems = foodItems,
            updateCanvasSize = gameState::updateSize
        )
        GameController(
            modifier = modifier, performGameScreenAction = performGameScreenAction
        )
        Text(
            text = gameState.score.collectAsState(initial = 0).value.toString(),

            )
    }
}

private val TAG = "GameScreen"

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ScoreCounter(modifier: Modifier, value: Int) {
    Row(modifier = modifier) {
        value.toString().forEach { digit ->
            AnimatedContent(
                modifier = Modifier,
                targetState = digit.toString().toInt(),
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInVertically { -it } with slideOutVertically { it }
                    } else {
                        slideInVertically { it } with slideOutVertically { -it }
                    }
                }, label = "Counter animation"
            ) {
                Text(
                    text = it.toString(),
                    fontSize = 24.sp,
                    modifier = Modifier,
                )
            }
        }
    }

}

@Composable
private fun GameCanvas(
    modifier: Modifier,
    snekBody: List<Pair<Int, Int>>,
    foodItems: List<Pair<Int, Int>>,
    updateCanvasSize: (Int, Int) -> Unit,
) {
    val unitScale = 15
    Canvas(modifier = modifier.onGloballyPositioned { coordinates ->
        Log.d(TAG, "GameCanvas: ${coordinates.size}")
        updateCanvasSize(
            coordinates.size.width / unitScale, coordinates.size.height / unitScale
        )
    }) {
        snekBody.forEach {
            drawRect(
                color = Color.Black,
                topLeft = Offset(it.first.toFloat() * unitScale, it.second.toFloat() * unitScale),
                size = Size(width = unitScale.toFloat(), height = unitScale.toFloat())
            )
        }
        foodItems.forEach {
            drawCircle(
                color = Color.Red,
                center = Offset(it.first.toFloat() * unitScale, it.second.toFloat() * unitScale),
                radius = (unitScale / 2).toFloat(),
            )
        }
    }
}

@Composable
fun GameController(
    modifier: Modifier, performGameScreenAction: (GameScreenAction) -> Unit
) {
    Column(modifier) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .clickable { performGameScreenAction(GameScreenAction.MoveUp) }) {}
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Box(modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .clickable { performGameScreenAction(GameScreenAction.MoveLeft) }) {}
            Box(modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .clickable { performGameScreenAction(GameScreenAction.MoveRight) }) {

            }
        }
        Box(modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .clickable { performGameScreenAction(GameScreenAction.MoveDown) }) {}
    }
}