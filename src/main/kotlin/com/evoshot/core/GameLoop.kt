package com.evoshot.core

import com.evoshot.core.handler.EvoShotMessageHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class GameLoop(
    private val handler: EvoShotMessageHandler,
    private val tickRate: Int = 60,
) {
    private val logger = LoggerFactory.getLogger(GameLoop::class.java)
    private val running = AtomicBoolean(false)
    private val tickCount = AtomicLong(0)
    private val tickIntervalMs = 1000L / tickRate

    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun start() {
        if (running.getAndSet(true)) return

        logger.info("Game loop started (tickRate=$tickRate, interval=${tickIntervalMs}ms)")

        job =
            scope.launch {
                while (running.get() && isActive) {
                    val startTime = System.currentTimeMillis()

                    val tick = tickCount.incrementAndGet()
                    handler.play(tick)

                    val elapsed = System.currentTimeMillis() - startTime
                    val sleepTime = tickIntervalMs - elapsed
                    if (sleepTime > 0) {
                        delay(sleepTime)
                    }
                }
            }
    }

    fun stop() {
        running.set(false)
        job?.cancel()
        logger.info("Game loop stopped (total ticks: ${tickCount.get()})")
    }

    fun isRunning(): Boolean = running.get()

    fun getCurrentTick(): Long = tickCount.get()
}
