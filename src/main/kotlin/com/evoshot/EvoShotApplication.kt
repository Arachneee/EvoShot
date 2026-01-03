package com.evoshot

import com.evoshot.core.game.GameLoop
import com.evoshot.infra.server.GameServer
import com.evoshot.infra.server.StaticFileServer
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread

fun main() {
    val logger = LoggerFactory.getLogger("EvoShot")

    val gameServer = GameServer(port = 8080)
    val staticFileServer = StaticFileServer(port = 80)
    val gameLoop = GameLoop(gameServer.room, tickRate = 60)

    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info("Received shutdown signal")
        gameLoop.stop()
        gameServer.shutdown()
        staticFileServer.shutdown()
    })

    gameLoop.start()

    thread { staticFileServer.start() }

    gameServer.start()
}
