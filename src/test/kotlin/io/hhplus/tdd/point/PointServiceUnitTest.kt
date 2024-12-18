package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class PointServiceUnitTest {

    @Mock
    private lateinit var userPointTable: UserPointTable

    @Mock
    private lateinit var pointHistoryTable: PointHistoryTable

    @Mock
    private lateinit var pointValidator: PointValidator

    @InjectMocks
    private lateinit var pointService: PointService

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

    /**
     * 포인트 내역 조회 정상 동작 테스트
     * mock을 통한 포인트 조회 로직의 상태 및 행위 검증
     */
    @Test
    fun `id를 통해 포인트 내역을 조회한다`() {
        // given
        val userId = 11L
        val pointHistory1 = PointHistory(1L, userId, TransactionType.CHARGE, 1000L, 23L)
        val pointHistory2 = PointHistory(2L, userId, TransactionType.USE, 800L, 23L)
        `when`(pointService.getUserPointHistory(userId))
            .thenReturn(listOf(pointHistory1, pointHistory2))

        // when
        val actual = pointService.getUserPointHistory(userId)

        // then
        verify(pointHistoryTable).selectAllByUserId(11L)

        assertThat(actual).hasSize(2)

        assertThat(actual[0].id).isEqualTo(1L)
        assertThat(actual[0].userId).isEqualTo(11L)
        assertThat(actual[0].type).isEqualTo(TransactionType.CHARGE)
        assertThat(actual[0].amount).isEqualTo(1000L)

        assertThat(actual[1].id).isEqualTo(2L)
        assertThat(actual[1].userId).isEqualTo(11L)
        assertThat(actual[1].type).isEqualTo(TransactionType.USE)
        assertThat(actual[1].amount).isEqualTo(800L)
    }

    /**
     * 포인트 충전에 대한 정상 동작 테스트
     */
    @Test
    fun `포인트 충전 성공`() {
        // given
        val userId = 21L
        val existingPoint = 800L
        val amount = 300L

        val existingUserPoint = UserPoint(userId, existingPoint, 10L)
        val updatedUserPoint = UserPoint(userId, existingPoint + amount, 20L)

        // stubbing
        `when`(userPointTable.selectById(userId)).thenReturn(existingUserPoint)
        `when`(userPointTable.insertOrUpdate(userId, existingPoint + amount)).thenReturn(updatedUserPoint)

        // when
        val actual = pointService.chargePoint(userId, amount)

        //then
        verify(userPointTable).selectById(21L)
        verify(pointValidator).validateChargeable(800L, 300L)
        verify(userPointTable).insertOrUpdate(12L, 1100L)
        verify(pointHistoryTable).insert(userId, amount, TransactionType.CHARGE, 20L)

        assertThat(actual.id).isEqualTo(21L)
        assertThat(actual.point).isEqualTo(1100L)
        assertThat(actual.updateMillis).isEqualTo(20L)
    }

    /**
     * 포인트 충전 시 예외가 발생한 경우
     */
    @Test
    fun `포인트 충전 실패`() {
        // given
        val userId = 22L
        val existingPoint = 900_000L
        val amount = 130_000L
        val userPoint = UserPoint(userId, existingPoint, 13L)

        // stubbing
        `when`(userPointTable.selectById(userId)).thenReturn(userPoint)
        `when`(pointValidator.validateChargeable(existingPoint, amount))
            .thenThrow(RuntimeException("충전 실패!"))

        // when
        assertThatThrownBy { pointService.chargePoint(userId, amount) }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage("충전 실패!")

        //then
        verify(userPointTable).selectById(22L)
        verifyNoMoreInteractions(userPointTable)
        verifyNoInteractions(pointHistoryTable)
    }
}