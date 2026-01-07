package com.evoshot.core.infra.repository

import com.evoshot.core.domain.Bullet
import com.evoshot.core.domain.BulletRepository
import java.util.concurrent.ConcurrentHashMap

class InMemoryBulletRepository : BulletRepository {
    private val bullets = ConcurrentHashMap<String, Bullet>()

    override fun add(bullet: Bullet) {
        bullets[bullet.id] = bullet
    }

    override fun remove(bulletId: String): Bullet? = bullets.remove(bulletId)

    override fun getAll(): List<Bullet> = bullets.values.toList()

    override fun replaceAll(bullets: List<Bullet>) {
        this.bullets.clear()
        bullets.forEach { bullet ->
            this.bullets[bullet.id] = bullet
        }
    }
}
