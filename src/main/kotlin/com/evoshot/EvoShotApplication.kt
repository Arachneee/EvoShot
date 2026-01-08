package com.evoshot

import com.evoshot.core.controller.GameController
import com.evoshot.core.controller.message.JsonMessageCodec
import com.evoshot.core.domain.RoomManager
import com.evoshot.core.engine.GameEngine
import com.evoshot.core.engine.GameLoop
import com.evoshot.network.server.GameServer
import com.evoshot.network.server.StaticFileServer
import com.evoshot.network.session.SessionManager
import com.evoshot.network.session.SessionMessageBroadcaster
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread

fun main() {
    val logger = LoggerFactory.getLogger("EvoShot")

    val gameEngine = GameEngine()
    val roomManager = RoomManager(gameEngine = gameEngine)

    val codec = JsonMessageCodec()
    val sessionManager = SessionManager()
    val broadcaster = SessionMessageBroadcaster(sessionManager)
    val gameController = GameController(roomManager = roomManager, codec = codec, broadcaster = broadcaster)
    val gameServer = GameServer(port = 8080, sessionManager = sessionManager, messageHandler = gameController)

    val staticFileServer = StaticFileServer(port = 80)
    val gameLoop = GameLoop(tickHandler = gameController, tickRate = 60)

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
