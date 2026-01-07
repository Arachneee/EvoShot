package com.evoshot.network.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.slf4j.LoggerFactory

class StaticFileServer(
    private val port: Int = 80,
) {
    private val logger = LoggerFactory.getLogger(StaticFileServer::class.java)

    private val bossGroup = NioEventLoopGroup(1)
    private val workerGroup = NioEventLoopGroup()
    private var channel: Channel? = null

    fun start() {
        try {
            val bootstrap = ServerBootstrap()
            bootstrap
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(StaticFileServerInitializer())

            channel = bootstrap.bind(port).sync().channel()
            logger.info("Static file server started on port $port")
            logger.info("Test client: http://localhost:$port/")
        } catch (e: Exception) {
            logger.error("Failed to start static file server on port $port", e)
            shutdown()
        }
    }

    fun shutdown() {
        logger.info("Shutting down static file server...")
        channel?.close()
        workerGroup.shutdownGracefully()
        bossGroup.shutdownGracefully()
        logger.info("Static file server stopped")
    }
}
