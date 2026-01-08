package com.evoshot.core.domain

class Player private constructor(
    val sessionId: String,
    private var state: PlayerState,
) {
    val id: String get() = state.id
    val name: String get() = state.name
    val x: Float get() = state.x
    val y: Float get() = state.y
    val isAlive: Boolean get() = state.isAlive

    fun move(
        tx: Float,
        ty: Float,
    ) {
        this.state = state.move(tx, ty)
    }

    fun kill() {
        this.state = state.killed()
    }

    fun toState(): PlayerState = state

    companion object {
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
            isAlive: Boolean = true,
        ): Player =
            Player(
                sessionId,
                PlayerState.createForTest(id, name, x, y, isAlive),
            )
    }
}
