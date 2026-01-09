package com.evoshot.core.domain

class Player private constructor(
    val sessionId: String,
    private var state: PlayerState,
) {
    val id: String get() = state.id
    val name: String get() = state.name
    val x: Float get() = state.x
    val y: Float get() = state.y
    val hp: Int get() = state.hp
    val isAlive: Boolean get() = state.isAlive

    private var inputDx: Int = 0
    private var wantsToJump: Boolean = false
    private var lastShootTime: Long = 0

    fun setInput(
        dx: Int,
        jump: Boolean,
    ) {
        this.inputDx = dx
        this.wantsToJump = jump
    }

    fun applyInput() {
        state = state.move(inputDx)
        if (wantsToJump) {
            state = state.jump()
            wantsToJump = false
        }
        state = state.applyGravity()
    }

    fun canShoot(currentTime: Long = System.currentTimeMillis()): Boolean =
        currentTime - lastShootTime >= SHOOT_COOLDOWN_MS

    fun recordShoot(currentTime: Long = System.currentTimeMillis()) {
        this.lastShootTime = currentTime
    }

    fun takeDamage(damage: Int = 1) {
        this.state = state.takeDamage(damage)
    }

    fun toState(): PlayerState = state

    companion object {
        const val SHOOT_COOLDOWN_MS: Long = 200

        fun create(
            sessionId: String,
            playerName: String,
        ): Player =
            Player(
                sessionId,
                PlayerState.create(
                    name = playerName,
                ),
            )

        internal fun createForTest(
            sessionId: String,
            id: String,
            name: String,
            x: Float,
            y: Float,
            velocityY: Float = 0f,
            hp: Int = PlayerState.MAX_HP,
        ): Player =
            Player(
                sessionId,
                PlayerState.createForTest(id, name, x, y, velocityY, hp),
            )
    }
}
