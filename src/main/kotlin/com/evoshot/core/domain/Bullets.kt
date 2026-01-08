package com.evoshot.core.domain

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class Bullets {
    private val bullets = ConcurrentHashMap<String, Bullet>()
    private val idCounter = AtomicLong(0)

    fun createAndAdd(
        x: Float,
        y: Float,
        tx: Float,
        ty: Float,
    ) {
        val bullet = Bullet.create(
            id = idCounter.incrementAndGet().toString(),
            x = x,
            y = y,
            tx = tx,
            ty = ty,
        )
        bullets[bullet.id] = bullet
    }

    fun remove(bulletId: String): Bullet? = bullets.remove(bulletId)

    fun getAll(): List<Bullet> = bullets.values.toList()

    fun replaceAll(newBullets: List<Bullet>) {
        bullets.clear()
        bullets.putAll(newBullets.associateBy { it.id })
    }

    fun removeAll(ids: Set<String>) {
        ids.forEach { bullets.remove(it) }
    }
}

