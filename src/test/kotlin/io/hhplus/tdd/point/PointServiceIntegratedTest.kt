package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class PointServiceIntegratedTest(
    @Autowired val userPointTable: UserPointTable,
    @Autowired val pointHistoryTable: PointHistoryTable,
    @Autowired val pointService: PointService
) {
    @Nested
    inner class `포인트 조회 통합 테스트` {
        /**
         * 정상 동작에 대한 테스트
         */
        @Test
        fun `101 포인트를 가진 유저를 조회하면 101 포인트가 조회된다`() {
            // given
            userPointTable.insertOrUpdate(101L, 1500L)

            // when
            val actual = pointService.getUserPoint(101L)

            // then
            assertThat(actual.id).isEqualTo(101L)
            assertThat(actual.point).isEqualTo(1500L)
        }

        /**
         * 사용한 적이 없는 유저의 id로 조회하는 예외 케이스 테스트
         */
        @Test
        fun `저장되지 않은 유저로 조회하면 0 포인트로 조회된다`() {
            // given

            // when
            val actual = pointService.getUserPoint(102L)

            // then
            assertThat(actual.id).isEqualTo(102L)
            assertThat(actual.point).isEqualTo(0L)
        }
    }

    @Nested
    inner class `포인트 내역 조회 통합 테스트` {
        /**
         * 정상 동작에 대한 테스트
         */
        @Test
        fun `여러 포인트 사용 기록 중 조회한 유저의 포인트 내역만 조회해온다`() {
            // given
            pointHistoryTable.insert(111L, 800L, TransactionType.CHARGE, 30L)
            pointHistoryTable.insert(111L, 300L, TransactionType.USE, 80L)
            pointHistoryTable.insert(112L, 1000L, TransactionType.CHARGE, 10L)
            pointHistoryTable.insert(113L, 150L, TransactionType.CHARGE, 100L)

            // when
            val actual = pointService.getUserPointHistory(111L)

            // then
            assertThat(actual).hasSize(2)

            assertThat(actual[0].userId).isEqualTo(111L)
            assertThat(actual[0].amount).isEqualTo(800L)
            assertThat(actual[0].type).isEqualTo(TransactionType.CHARGE)
            assertThat(actual[0].timeMillis).isEqualTo(30L)

            assertThat(actual[1].userId).isEqualTo(111L)
            assertThat(actual[1].amount).isEqualTo(300L)
            assertThat(actual[1].type).isEqualTo(TransactionType.USE)
            assertThat(actual[1].timeMillis).isEqualTo(80L)
        }

        /**
         * 사용한 적이 없는 유저의 id의 포인트 내역을 조회하는 예외 케이스 테스트
         */
        @Test
        fun `저장되지 않은 유저로 포인트 내역을 조회 시 빈 리스트가 반환된다`() {
            // given

            // when
            val actual = pointService.getUserPointHistory(114L)

            // then
            assertThat(actual).isEmpty()
        }
    }

    @Nested
    inner class `포인트 충전 통합 테스트` {
        /**
         * 포인트 충전 정상 동작에 대한 테스트
         * userPointTable과 pointHistoryTable 클래스의 삽입, 조회 메서드는 무결한 동작임을 가정하고 작성
         */
        @Test
        fun `1500 포인트를 가진 유저가 710 포인트를 충전하면 2210 포인트가 저장되어 있고 포인트 충전 기록이 추가된다`() {
            // given
            val userId = 121L
            userPointTable.insertOrUpdate(userId, 1500L)

            // when
            val actual1 = pointService.chargePoint(userId, 710L)
            val actual2 = pointHistoryTable.selectAllByUserId(userId)

            //then
            assertThat(actual1.id).isEqualTo(userId)
            assertThat(actual1.point).isEqualTo(2210L)

            assertThat(actual2).hasSize(1)

            val set = actual2[0]
            assertThat(set.userId).isEqualTo(121L)
            assertThat(set.amount).isEqualTo(710L)
            assertThat(set.type).isEqualTo(TransactionType.CHARGE)
        }
    }

    @Nested
    inner class `포인트 사용 통합 테스트` {
        /**
         * 포인트 사용 정상 동작에 대한 테스트
         * userPointTable과 pointHistoryTable 클래스의 삽입, 조회 메서드는 무결한 동작임을 가정하고 작성
         */
        @Test
        fun `2000 포인트를 가진 유저가 170 포인트를 사용하면 1830 포인트가 저장되어 있고 포인트 사용 기록이 추가된다`() {
            // given
            val userId = 131L
            userPointTable.insertOrUpdate(userId, 2000L)

            // when
            val actual1 = pointService.usePoint(userId, 170L)
            val actual2 = pointHistoryTable.selectAllByUserId(userId)

            //then
            assertThat(actual1.id).isEqualTo(131L)
            assertThat(actual1.point).isEqualTo(1830L)

            assertThat(actual2).hasSize(1)

            val set = actual2[0]
            assertThat(set.userId).isEqualTo(131L)
            assertThat(set.amount).isEqualTo(170L)
            assertThat(set.type).isEqualTo(TransactionType.USE)
        }
    }
}