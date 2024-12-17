package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PointServiceTest {

    private val userPointTable = UserPointTable()
    private val pointHistoryTable = PointHistoryTable()
    private val sut = PointService(userPointTable, pointHistoryTable)

    /**
     * 포인트 충전에 대한 통합 테스트
     */
    @Nested
    inner class `포인트 충전` {
        /**
         * 정상 동작에 대한 테스트
         */
        @Test
        fun `id를 통해 포인트를 조회할 수 있다`() {
            userPointTable.insertOrUpdate(101L, 1500L)

            val actual = sut.getUserPoint(101L)

            assertThat(actual.id).isEqualTo(101L)
            assertThat(actual.point).isEqualTo(1500L)
        }

        /**
         * 사용한 적이 없는 유저의 id로 조회하는 예외 케이스 테스트
         */
        @Test
        fun `없는 id로 조회하면 0 포인트로 조회된다`() {

            val actual = sut.getUserPoint(102L)

            assertThat(actual.id).isEqualTo(102L)
            assertThat(actual.point).isEqualTo(0L)
        }
    }
}