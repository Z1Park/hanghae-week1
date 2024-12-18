package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class PointServiceUnitTest {

    /**
     * 포인트 조회에 대한 단위 테스트
     */
    @Nested
    inner class `포인트 조회` {
        /**
         * 포인트 조회의 경우 UserPointTable의 메서드를 호출하여 반환하는 로직으로,
         * 로직이 단순하여 Stub을 통한 상태 검증보다 Mock을 통한 행위 검증으로 테스트
         */
        @Test
        fun `UserPointTable의 조회 메서드를 호출한다`() {
            // given
            val userPointTable = mock(UserPointTable::class.java)
            val pointHistoryTable = mock(PointHistoryTable::class.java)
            val sut = PointService(userPointTable, pointHistoryTable)

            // when
            sut.getUserPoint(1L)

            // then
            verify(userPointTable, times(1)).selectById(1L)
            verifyNoMoreInteractions(userPointTable)
            verifyNoInteractions(pointHistoryTable)
        }
    }

    @Nested
    inner class `포인트 내역 조회` {
        /**
         * 포인트 내역 조회의 경우 PointHistoryTable의 메서드를 호출하여 반환하는 로직으로,
         * 로직이 단순하여 Stub을 통한 상태 검증보다 Mock을 통한 행위 검증으로 테스트
         */
        @Test
        fun `PointHistoryTable의 조회 메서드를 호출한다`() {
            // given
            val userPointTable = mock(UserPointTable::class.java)
            val pointHistoryTable = mock(PointHistoryTable::class.java)
            val sut = PointService(userPointTable, pointHistoryTable)

            // when
            sut.getUserPointHistory(11L)

            // then
            verify(pointHistoryTable, times(1)).selectAllByUserId(11L)
            verifyNoMoreInteractions(pointHistoryTable)
            verifyNoInteractions(userPointTable)
        }
    }
}