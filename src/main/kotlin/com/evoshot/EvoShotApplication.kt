package com.evoshot

import com.evoshot.core.game.GameLoop
import com.evoshot.infra.server.GameServer
import org.slf4j.LoggerFactory

fun main() {
    val logger = LoggerFactory.getLogger("EvoShot")

    val server = GameServer(port = 8080)
    val gameLoop = GameLoop(server.room, tickRate = 60)

    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info("Received shutdown signal")
        gameLoop.stop()
        server.shutdown()
    })

    gameLoop.start()
    server.start()
}
