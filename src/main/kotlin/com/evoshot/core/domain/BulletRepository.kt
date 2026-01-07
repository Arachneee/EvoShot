package com.evoshot.core.domain

interface BulletRepository {
    fun add(bullet: Bullet)
    fun remove(bulletId: String): Bullet?
    fun getAll(): List<Bullet>
    fun replaceAll(bullets: List<Bullet>)
}
