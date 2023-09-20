package com.ducktappedapps.snek

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlin.random.Random

class GameState {
    sealed class GameEvent {
        object SnekHitAWall : GameEvent()
        object SnekBitItself : GameEvent()
    }

    private val TAG = "GameState"
    private val initialSnekBodySize = 10
    private var moveDown: Boolean? = null
    private var moveLeft: Boolean? = null
    private var canvasHeight: Int = 0
    private var canvasWidth: Int = 0

    val gameEvent: MutableSharedFlow<GameEvent> = MutableSharedFlow()
    val snekSparseArray: MutableStateFlow<List<Pair<Int, Int>>> = MutableStateFlow(listOf())
    val foodSparseArray: MutableStateFlow<List<Pair<Int, Int>>> = MutableStateFlow(listOf())
    val score: Flow<Int> = snekSparseArray.map { it.size - initialSnekBodySize }
    val snekSpeed: MutableStateFlow<Float> = MutableStateFlow(0.5f)

    fun updateSize(width: Int, height: Int) {
        Log.d(TAG, "Canvas Size updated: ${width}x${height}")
        if (canvasWidth != width && canvasHeight != height) {
            canvasWidth = width
            canvasHeight = height
            populateFoodInEnvironment()
            spawnSnek()
        }

    }

    private fun spawnSnek() {
        Log.d(TAG, "spawnSnek: Populating snek body")
        snekSparseArray.value = buildList {
            repeat(initialSnekBodySize) {
                add(canvasWidth / 2 + it to canvasHeight / 2)
            }
        }

        moveLeft = true
        moveDown = null
    }

    private fun populateFoodInEnvironment() {
        for (i in 0..100) {
            addFoodItemToEnvironment()
        }
    }

    fun moveUp() {
        if (moveLeft != null) {
            moveDown = false
            moveLeft = null
        }
    }

    fun moveDown() {
        if (moveLeft != null) {
            moveDown = true
            moveLeft = null
        }
    }

    fun moveLeft() {
        if (moveDown != null) {
            moveDown = null
            moveLeft = true
        }
    }

    fun moveRight() {
        if (moveDown != null) {
            moveDown = null
            moveLeft = false
        }
    }

    suspend fun updateStateOnLoopTick() {
        updateSnekBody()
    }

    private fun addFoodItemToEnvironment() {
        foodSparseArray.value = foodSparseArray.value + listOf(
            Random.nextInt(canvasWidth) to Random.nextInt(canvasHeight)
        )
    }

    private suspend fun updateSnekBody() {
        if (isSneakHittingEnvironment()) {
            moveDown = null
            moveLeft = null
            gameEvent.emit(GameEvent.SnekHitAWall)
        } else if (isSnekConsumingFood()) {
            snekSparseArray.value = listOf(
                when {
                    moveDown == true -> snekSparseArray.value[0].first to snekSparseArray.value[0].second + 1


                    moveDown == false -> snekSparseArray.value[0].first to snekSparseArray.value[0].second - 1


                    moveLeft == true -> snekSparseArray.value[0].first - 1 to snekSparseArray.value[0].second


                    else -> snekSparseArray.value[0].first + 1 to snekSparseArray.value[0].second
                }
            ) + snekSparseArray.value.toList()
            addFoodItemToEnvironment()
        } else if (isSneBitingItself()) {
            moveDown = null
            moveLeft = null
            gameEvent.emit(GameEvent.SnekBitItself)
        } else {
            if (snekSparseArray.value.isNotEmpty())
                snekSparseArray.value = listOf(
                    when {
                        moveDown == true -> snekSparseArray.value[0].first to snekSparseArray.value[0].second + 1


                        moveDown == false -> snekSparseArray.value[0].first to snekSparseArray.value[0].second - 1


                        moveLeft == true -> snekSparseArray.value[0].first - 1 to snekSparseArray.value[0].second


                        else -> snekSparseArray.value[0].first + 1 to snekSparseArray.value[0].second
                    }
                ) + snekSparseArray.value.toList().dropLast(1)
        }


    }

    private fun isSneBitingItself(): Boolean {
        return snekSparseArray
            .value
            .drop(1)
            .any { it.first == snekSparseArray.value[0].first && it.second == snekSparseArray.value[0].second }

    }

    private fun isSnekConsumingFood(): Boolean {
        return if (moveDown == true && foodSparseArray.value.contains(snekSparseArray.value[0].first to snekSparseArray.value[0].second + 1)) {
            foodSparseArray.value =
                foodSparseArray.value.filterNot { it == snekSparseArray.value[0].first to snekSparseArray.value[0].second + 1 }
            true
        } else if (moveDown == false && foodSparseArray.value.contains(snekSparseArray.value[0].first to snekSparseArray.value[0].second - 1)) {
            foodSparseArray.value =
                foodSparseArray.value.filterNot { it == snekSparseArray.value[0].first to snekSparseArray.value[0].second - 1 }
            true
        } else if (moveLeft == true && foodSparseArray.value.contains(snekSparseArray.value[0].first - 1 to snekSparseArray.value[0].second)) {
            foodSparseArray.value =
                foodSparseArray.value.filterNot { it == snekSparseArray.value[0].first - 1 to snekSparseArray.value[0].second }
            true
        } else if (moveLeft == false && foodSparseArray.value.contains(snekSparseArray.value[0].first + 1 to snekSparseArray.value[0].second)) {
            foodSparseArray.value =
                foodSparseArray.value.filterNot { it == snekSparseArray.value[0].first + 1 to snekSparseArray.value[0].second }
            true
        } else if (moveLeft == null && moveDown == null) {
            false
        } else {
            false
        }
    }

    private fun isSneakHittingEnvironment(): Boolean {
        return when {
            moveDown == true && snekSparseArray.value[0].second + 1 == canvasHeight -> true
            moveDown == false && snekSparseArray.value[0].second - 1 == 0 -> true
            moveLeft == true && snekSparseArray.value[0].first - 1 == 0 -> true
            moveLeft == false && snekSparseArray.value[0].first + 1 == canvasWidth -> true
            else -> moveLeft == null && moveDown == null
        }
    }
}