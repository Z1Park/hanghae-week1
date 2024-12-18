package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class PointServiceUnitTest {

    @Mock
    private lateinit var userPointTable: UserPointTable

    @Mock
    private lateinit var pointHistoryTable: PointHistoryTable

    @InjectMocks
    private lateinit var pointService: PointService

    /**
     * 포인트 조회에 대한 단위 테스트
     */
    @Nested
    inner class `포인트 조회` {
        /**
         * 포인트 조회 정상 동작 테스트
         * mock을 통한 포인트 조회 로직의 상태 및 행위 검증
         */
        @Test
        fun `id를 통해 포인트를 조회한다`() {
            // given
            val userId = 1L
            val userPoint = UserPoint(userId, 1000L, 30L)
            `when`(userPointTable.selectById(userId)).thenReturn(userPoint)

            // when
            val actual = pointService.getUserPoint(userId)

            // then
            verify(userPointTable).selectById(1L)

            assertThat(actual.id).isEqualTo(1L)
            assertThat(actual.point).isEqualTo(1000L)
            assertThat(actual.updateMillis).isEqualTo(30L)
        }
    }

    @Nested
    inner class `포인트 내역 조회` {
        /**
         * 포인트 내역 조회 정상 동작 테스트
         * mock을 통한 포인트 조회 로직의 상태 및 행위 검증
         */
        @Test
        fun `PointHistoryTable의 조회 메서드를 호출한다`() {
            // given
            val userId = 11L
            val pointHistory1 = PointHistory(1L, userId, TransactionType.CHARGE, 1000L, 23L)
            val pointHistory2 = PointHistory(2L, userId, TransactionType.USE, 800L, 23L)
            `when`(pointService.getUserPointHistory(userId))
                .thenReturn(listOf(pointHistory1, pointHistory2))

            // when
            val userPointHistory = pointService.getUserPointHistory(userId)

            // then
            verify(pointHistoryTable).selectAllByUserId(11L)

            assertThat(userPointHistory).hasSize(2)

            assertThat(userPointHistory[0].id).isEqualTo(1L)
            assertThat(userPointHistory[0].userId).isEqualTo(11L)
            assertThat(userPointHistory[0].type).isEqualTo(TransactionType.CHARGE)
            assertThat(userPointHistory[0].amount).isEqualTo(1000L)

            assertThat(userPointHistory[1].id).isEqualTo(2L)
            assertThat(userPointHistory[1].userId).isEqualTo(11L)
            assertThat(userPointHistory[1].type).isEqualTo(TransactionType.USE)
            assertThat(userPointHistory[1].amount).isEqualTo(800L)
        }
    }
}