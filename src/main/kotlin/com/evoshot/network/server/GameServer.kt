package com.evoshot.network.server

import com.evoshot.network.handler.MessageHandler
import com.evoshot.network.session.SessionManager
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.ServerSocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.slf4j.LoggerFactory

class GameServer(
    private val port: Int = 8080,
    private val sessionManager: SessionManager,
    private val messageHandler: MessageHandler,
) {
    private val logger = LoggerFactory.getLogger(GameServer::class.java)

    private val useEpoll = Epoll.isAvailable()
    private val bossGroup: EventLoopGroup = if (useEpoll) EpollEventLoopGroup(1) else NioEventLoopGroup(1)
    private val workerGroup: EventLoopGroup = if (useEpoll) EpollEventLoopGroup() else NioEventLoopGroup()
    private val serverChannelClass: Class<out ServerSocketChannel> =
        if (useEpoll) EpollServerSocketChannel::class.java else NioServerSocketChannel::class.java
    private var channel: Channel? = null

    fun start() {
        logger.info("Using ${if (useEpoll) "Epoll" else "NIO"} transport")
        try {
            val bootstrap = ServerBootstrap()
            bootstrap
                .group(bossGroup, workerGroup)
                .channel(serverChannelClass)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(GameServerInitializer(sessionManager, messageHandler))

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
