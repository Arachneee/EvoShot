package com.evoshot.core.engine

import com.evoshot.core.domain.Bullet
import com.evoshot.core.domain.Player
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GameEngineTest {
    private val gameEngine = GameEngine()

    @Nested
    inner class MoveAndFilterBullets {
        @Test
        fun `빈 리스트는 빈 리스트를 반환한다`() {
            val bullets = emptyList<Bullet>()

            val result = gameEngine.moveAndFilterBullets(bullets)

            assertThat(result).isEmpty()
        }

        @Test
        fun `총알이 이동한다`() {
            val bullet = Bullet.create(
                id = "bullet-1",
                ownerId = "player-1",
                x = 100f,
                y = 100f,
                tx = 200f,
                ty = 100f,
            )

            val result = gameEngine.moveAndFilterBullets(listOf(bullet))

            assertThat(result).hasSize(1)
            assertThat(result.first().x).isGreaterThan(bullet.x)
        }

        @Test
        fun `월드 왼쪽 경계를 벗어난 총알은 필터링된다`() {
            val bullet = Bullet.create(
                id = "bullet-1",
                ownerId = "player-1",
                x = 5f,
                y = 100f,
                tx = -100f,
                ty = 100f,
            )

            val result = gameEngine.moveAndFilterBullets(listOf(bullet))

            assertThat(result).isEmpty()
        }

        @Test
        fun `월드 오른쪽 경계를 벗어난 총알은 필터링된다`() {
            val bullet = Bullet.create(
                id = "bullet-1",
                ownerId = "player-1",
                x = GameEngine.WORLD_WIDTH - 5f,
                y = 100f,
                tx = GameEngine.WORLD_WIDTH + 100f,
                ty = 100f,
            )

            val result = gameEngine.moveAndFilterBullets(listOf(bullet))

            assertThat(result).isEmpty()
        }

        @Test
        fun `월드 상단 경계를 벗어난 총알은 필터링된다`() {
            val bullet = Bullet.create(
                id = "bullet-1",
                ownerId = "player-1",
                x = 100f,
                y = 5f,
                tx = 100f,
                ty = -100f,
            )

            val result = gameEngine.moveAndFilterBullets(listOf(bullet))

            assertThat(result).isEmpty()
        }

        @Test
        fun `월드 하단 경계를 벗어난 총알은 필터링된다`() {
            val bullet = Bullet.create(
                id = "bullet-1",
                ownerId = "player-1",
                x = 100f,
                y = GameEngine.WORLD_HEIGHT - 5f,
                tx = 100f,
                ty = GameEngine.WORLD_HEIGHT + 100f,
            )

            val result = gameEngine.moveAndFilterBullets(listOf(bullet))

            assertThat(result).isEmpty()
        }

        @Test
        fun `바닥에 닿은 총알은 필터링된다`() {
            val bullet = Bullet.create(
                id = "bullet-1",
                ownerId = "player-1",
                x = 100f,
                y = Physics.GROUND_Y - 5f,
                tx = 100f,
                ty = Physics.GROUND_Y + 100f,
            )

            val result = gameEngine.moveAndFilterBullets(listOf(bullet))

            assertThat(result).isEmpty()
        }

        @Test
        fun `월드 내부에 있는 총알은 유지된다`() {
            val bullet = Bullet.create(
                id = "bullet-1",
                ownerId = "player-1",
                x = 500f,
                y = 500f,
                tx = 600f,
                ty = 500f,
            )

            val result = gameEngine.moveAndFilterBullets(listOf(bullet))

            assertThat(result).hasSize(1)
        }

        @Test
        fun `여러 총알 중 경계를 벗어난 총알만 필터링된다`() {
            val insideBullet = Bullet.create(
                id = "bullet-1",
                ownerId = "player-1",
                x = 500f,
                y = 500f,
                tx = 600f,
                ty = 500f,
            )
            val outsideBullet = Bullet.create(
                id = "bullet-2",
                ownerId = "player-1",
                x = 5f,
                y = 100f,
                tx = -100f,
                ty = 100f,
            )

            val result = gameEngine.moveAndFilterBullets(listOf(insideBullet, outsideBullet))

            assertThat(result).hasSize(1)
            assertThat(result.first().id).isEqualTo("bullet-1")
        }
    }

    @Nested
    inner class CheckHits {
        @Test
        fun `빈 플레이어 리스트는 빈 결과를 반환한다`() {
            val bullet = Bullet.create(
                id = "bullet-1",
                ownerId = "player-1",
                x = 100f,
                y = 100f,
                tx = 200f,
                ty = 100f,
            )

            val result = gameEngine.checkHits(emptyList(), listOf(bullet))

            assertThat(result.hitPlayerIds).isEmpty()
            assertThat(result.hitBulletIds).isEmpty()
        }

        @Test
        fun `빈 총알 리스트는 빈 결과를 반환한다`() {
            val player = Player.createForTest(
                sessionId = "session-1",
                id = "player-1",
                name = "홍길동",
                x = 100f,
                y = 100f,
            )

            val result = gameEngine.checkHits(listOf(player), emptyList())

            assertThat(result.hitPlayerIds).isEmpty()
            assertThat(result.hitBulletIds).isEmpty()
        }

        @Test
        fun `플레이어와 총알이 같은 위치에 있으면 히트로 판정한다`() {
            val player = Player.createForTest(
                sessionId = "session-1",
                id = "player-1",
                name = "홍길동",
                x = 100f,
                y = 100f,
            )
            val bullet = Bullet.create(
                id = "bullet-1",
                ownerId = "other-player",
                x = 100f,
                y = 100f,
                tx = 200f,
                ty = 100f,
            )

            val result = gameEngine.checkHits(listOf(player), listOf(bullet))

            assertThat(result.hitPlayerIds).contains("player-1")
            assertThat(result.hitBulletIds).contains("bullet-1")
        }

        @Test
        fun `플레이어와 총알이 히트 반경 내에 있으면 히트로 판정한다`() {
            val player = Player.createForTest(
                sessionId = "session-1",
                id = "player-1",
                name = "홍길동",
                x = 100f,
                y = 100f,
            )
            val bullet = Bullet.create(
                id = "bullet-1",
                ownerId = "other-player",
                x = 105f,
                y = 105f,
                tx = 200f,
                ty = 100f,
            )

            val result = gameEngine.checkHits(listOf(player), listOf(bullet))

            assertThat(result.hitPlayerIds).contains("player-1")
            assertThat(result.hitBulletIds).contains("bullet-1")
        }

        @Test
        fun `플레이어와 총알이 히트 반경 밖에 있으면 히트로 판정하지 않는다`() {
            val player = Player.createForTest(
                sessionId = "session-1",
                id = "player-1",
                name = "홍길동",
                x = 100f,
                y = 100f,
            )
            val bullet = Bullet.create(
                id = "bullet-1",
                ownerId = "other-player",
                x = 200f,
                y = 200f,
                tx = 300f,
                ty = 200f,
            )

            val result = gameEngine.checkHits(listOf(player), listOf(bullet))

            assertThat(result.hitPlayerIds).isEmpty()
            assertThat(result.hitBulletIds).isEmpty()
        }

        @Test
        fun `하나의 총알은 한 명의 플레이어만 히트할 수 있다`() {
            val player1 = Player.createForTest(
                sessionId = "session-1",
                id = "player-1",
                name = "홍길동",
                x = 100f,
                y = 100f,
            )
            val player2 = Player.createForTest(
                sessionId = "session-2",
                id = "player-2",
                name = "김철수",
                x = 100f,
                y = 100f,
            )
            val bullet = Bullet.create(
                id = "bullet-1",
                ownerId = "other-player",
                x = 100f,
                y = 100f,
                tx = 200f,
                ty = 100f,
            )

            val result = gameEngine.checkHits(listOf(player1, player2), listOf(bullet))

            assertThat(result.hitPlayerIds).hasSize(1)
            assertThat(result.hitBulletIds).hasSize(1)
        }

        @Test
        fun `여러 총알이 각각 다른 플레이어를 히트할 수 있다`() {
            val player1 = Player.createForTest(
                sessionId = "session-1",
                id = "player-1",
                name = "홍길동",
                x = 100f,
                y = 100f,
            )
            val player2 = Player.createForTest(
                sessionId = "session-2",
                id = "player-2",
                name = "김철수",
                x = 500f,
                y = 500f,
            )
            val bullet1 = Bullet.create(
                id = "bullet-1",
                ownerId = "player-2",
                x = 100f,
                y = 100f,
                tx = 200f,
                ty = 100f,
            )
            val bullet2 = Bullet.create(
                id = "bullet-2",
                ownerId = "player-1",
                x = 500f,
                y = 500f,
                tx = 600f,
                ty = 500f,
            )

            val result = gameEngine.checkHits(listOf(player1, player2), listOf(bullet1, bullet2))

            assertThat(result.hitPlayerIds).containsExactlyInAnyOrder("player-1", "player-2")
            assertThat(result.hitBulletIds).containsExactlyInAnyOrder("bullet-1", "bullet-2")
        }

        @Test
        fun `플레이어 하나가 히트되면 해당 플레이어에 대한 다른 총알은 히트되지 않는다`() {
            val player = Player.createForTest(
                sessionId = "session-1",
                id = "player-1",
                name = "홍길동",
                x = 100f,
                y = 100f,
            )
            val bullet1 = Bullet.create(
                id = "bullet-1",
                ownerId = "other-player",
                x = 100f,
                y = 100f,
                tx = 200f,
                ty = 100f,
            )
            val bullet2 = Bullet.create(
                id = "bullet-2",
                ownerId = "other-player",
                x = 100f,
                y = 100f,
                tx = 200f,
                ty = 100f,
            )

            val result = gameEngine.checkHits(listOf(player), listOf(bullet1, bullet2))

            assertThat(result.hitPlayerIds).hasSize(1)
            assertThat(result.hitBulletIds).hasSize(1)
        }

        @Test
        fun `자신이 발사한 총알에는 맞지 않는다`() {
            val player = Player.createForTest(
                sessionId = "session-1",
                id = "player-1",
                name = "홍길동",
                x = 100f,
                y = 100f,
            )
            val bullet = Bullet.create(
                id = "bullet-1",
                ownerId = "player-1",
                x = 100f,
                y = 100f,
                tx = 200f,
                ty = 100f,
            )

            val result = gameEngine.checkHits(listOf(player), listOf(bullet))

            assertThat(result.hitPlayerIds).isEmpty()
            assertThat(result.hitBulletIds).isEmpty()
        }
    }
}

