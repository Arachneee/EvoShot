package com.evoshot.infra.server

import com.evoshot.core.room.Room
import com.evoshot.infra.codec.JsonMessageCodec
import com.evoshot.infra.codec.MessageCodec
import com.evoshot.infra.session.SessionManager
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.slf4j.LoggerFactory

class GameServer(
    private val port: Int = 8080,
    private val codec: MessageCodec = JsonMessageCodec(),
) {
    private val logger = LoggerFactory.getLogger(GameServer::class.java)

    private val bossGroup = NioEventLoopGroup(1)
    private val workerGroup = NioEventLoopGroup()
    private var channel: Channel? = null

    val sessionManager = SessionManager(codec)
    val room = Room(id = "global", maxPlayers = 2000)

    fun start() {
        try {
            val bootstrap = ServerBootstrap()
            bootstrap
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(GameServerInitializer(sessionManager, room, codec))

            channel = bootstrap.bind(port).sync().channel()
            logger.info("Game server started on port $port")
            logger.info("WebSocket endpoint: ws://localhost:$port/ws")

            channel?.closeFuture()?.sync()
        } finally {
            shutdown()
        }
    }

    fun shutdown() {
        logger.info("Shutting down game server...")
        channel?.close()
        workerGroup.shutdownGracefully()
        bossGroup.shutdownGracefully()
        logger.info("Game server stopped")
    }
}

