package com.evoshot

import com.evoshot.core.GameLoop
import com.evoshot.core.domain.Room
import com.evoshot.core.handler.EvoShotMessageHandler
import com.evoshot.core.handler.message.JsonMessageCodec
import com.evoshot.network.server.GameServer
import com.evoshot.network.server.StaticFileServer
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread

fun main() {
    val logger = LoggerFactory.getLogger("EvoShot")

    val codec = JsonMessageCodec()
    val room = Room(id = "global", maxPlayers = 2000)
    val messageHandler = EvoShotMessageHandler(room = room, codec = codec)
    val gameServer = GameServer(port = 8080, messageHandler = messageHandler)
    val staticFileServer = StaticFileServer(port = 80)
    val gameLoop = GameLoop(handler = messageHandler, tickRate = 60)

    Runtime.getRuntime().addShutdownHook(
        Thread {
            logger.info("Received shutdown signal")
            gameLoop.stop()
            gameServer.shutdown()
            staticFileServer.shutdown()
        },
    )

    gameLoop.start()

    thread { staticFileServer.start() }

    gameServer.start()
}
