package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@SpringBootTest
class PointServiceConcurrencyTest(
    @Autowired val userPointTable: UserPointTable,
    @Autowired val pointHistoryTable: PointHistoryTable,
    @Autowired val pointService: PointService
) {

    /**
     * 동일한 유저가 충전 요청을 여러 개 동시에 요청해도 정합성이 맞게 처리되는지 테스트
     */
    @Test
    fun `한 유저가 10 포인트 충전 요청을 동시에 1000번 보내면 10000 포인트가 충전되고 충전 기록이 1000개 남아있어야 한다`() {
        // given
        val userId = 201L
        val chargeAmount = 10L

        val requestCount = 1000
        val countDownLatch = CountDownLatch(requestCount)
        val executorService = Executors.newFixedThreadPool(requestCount)

        // when
        for (i in 1..requestCount) {
            executorService.execute() {
                pointService.chargePoint(userId, chargeAmount)
                countDownLatch.countDown()
            }
        }

        //then
        countDownLatch.await()

        val resultUserPoint = userPointTable.selectById(userId)
        assertThat(resultUserPoint.id).isEqualTo(201L)
        assertThat(resultUserPoint.point).isEqualTo(10000L)

        val resultPointHistories = pointHistoryTable.selectAllByUserId(userId)
        assertThat(resultPointHistories).hasSize(1000)
            .allMatch() { pointHistory -> pointHistory.amount == 10L }
            .allMatch() { pointHistory -> pointHistory.type == TransactionType.CHARGE }
    }

    /**
     * 서로 다른 유저가 한 여러 충전 요청에 대해 서로 Lock을 대기 하지 않는지 테스트
     */
    @Test
    fun `10명의 유저가 100ms 걸리는 100 포인트 충전 요청을 동시에 보내도 150ms 안에 실행되고 각각 100 포인트씩 저장되어 있다`() {
        // given
        var userId = 211L
        val requestUserCount = 10
        val chargeAmount = 100L

        val countDownLatch = CountDownLatch(requestUserCount)
        val executorService = Executors.newFixedThreadPool(requestUserCount)
        val startMillis = System.currentTimeMillis()

        // when
        for (i in 1..requestUserCount) {
            val currentUserId = userId + i
            executorService.execute() {
                pointService.chargePoint(currentUserId, chargeAmount)
                Thread.sleep(100)
                countDownLatch.countDown()
            }
        }

        //then
        countDownLatch.await()

        val endMillis = System.currentTimeMillis()
        assertThat(endMillis - startMillis).isLessThan(150)

        for (i in 1..requestUserCount) {
            val currentUserId = userId + i

            val resultUserPoint = userPointTable.selectById(currentUserId)
            assertThat(resultUserPoint.point).isEqualTo(100L)

            val resultPointHistories = pointHistoryTable.selectAllByUserId(currentUserId)
            assertThat(resultPointHistories).hasSize(1)
            assertThat(resultPointHistories[0].amount).isEqualTo(100L)
            assertThat(resultPointHistories[0].type).isEqualTo(TransactionType.CHARGE)
        }
    }

    /**
     * 동일한 유저가 사용 요청을 여러 개 동시에 요청해도 정합성이 맞게 처리되는지 테스트
     */
    @Test
    fun `20000 포인트를 가진 유저가 10 포인트 사용 요청을 동시에 1000번 보내면 10000 포인트가 남고 사용 기록이 1000개 남아있어야 한다`() {
        // given
        val userId = 241L
        val useAmount = 10L

        userPointTable.insertOrUpdate(userId, 20000L)

        val requestCount = 1000
        val countDownLatch = CountDownLatch(requestCount)
        val executorService = Executors.newFixedThreadPool(requestCount)

        // when
        for (i in 1..requestCount) {
            executorService.execute() {
                pointService.usePoint(userId, useAmount)
                countDownLatch.countDown()
            }
        }

        //then
        countDownLatch.await()

        val resultUserPoint = userPointTable.selectById(userId)
        assertThat(resultUserPoint.id).isEqualTo(241L)
        assertThat(resultUserPoint.point).isEqualTo(10000L)

        val resultPointHistories = pointHistoryTable.selectAllByUserId(userId)
        assertThat(resultPointHistories).hasSize(1000)
            .allMatch() { pointHistory -> pointHistory.amount == 10L }
            .allMatch() { pointHistory -> pointHistory.type == TransactionType.USE }
    }

    /**
     * 서로 다른 유저가 한 여러 사용 요청에 대해 서로 Lock을 대기 하지 않는지 테스트
     */
    @Test
    fun `200 포인트를 가진 10명의 유저가 100ms 걸리는 100 포인트 사용 요청을 동시에 보내도 150ms 안에 실행되고 각각 100 포인트씩 남아있다`() {
        // given
        var userId = 251L
        val requestUserCount = 10
        val useAmount = 100L

        for (i in 1..requestUserCount) {
            val currentUserId = userId + i
            userPointTable.insertOrUpdate(currentUserId, 200L)
        }

        val countDownLatch = CountDownLatch(requestUserCount)
        val executorService = Executors.newFixedThreadPool(requestUserCount)
        val startMillis = System.currentTimeMillis()

        // when
        for (i in 1..requestUserCount) {
            val currentUserId = userId + i
            executorService.execute() {
                pointService.usePoint(currentUserId, useAmount)
                Thread.sleep(100)
                countDownLatch.countDown()
            }
        }

        //then
        countDownLatch.await()

        val endMillis = System.currentTimeMillis()
        assertThat(endMillis - startMillis).isLessThan(150)

        for (i in 1..requestUserCount) {
            val currentUserId = userId + i

            val resultUserPoint = userPointTable.selectById(currentUserId)
            assertThat(resultUserPoint.point).isEqualTo(100L)

            val resultPointHistories = pointHistoryTable.selectAllByUserId(currentUserId)
            assertThat(resultPointHistories).hasSize(1)
            assertThat(resultPointHistories[0].amount).isEqualTo(100L)
            assertThat(resultPointHistories[0].type).isEqualTo(TransactionType.USE)
        }
    }

    /**
     * 포인트 충전과 사용을 통한 동시 요청의 처리 순서 테스트
     */
    @Test
    fun `800 포인트 충전, 1200 포인트 충전, 2000 포인트 사용 요청을 순차적으로 받으면 2번의 충전과 1번의 사용이 잘 이루어지고 0 포인트가 남는다`() {
        // given
        val userId = 281L
        val requestCount = 3

        val countDownLatch = CountDownLatch(requestCount)
        val executorService = Executors.newFixedThreadPool(requestCount)

        // when
        executorService.execute() {
            pointService.chargePoint(userId, 800L)
            countDownLatch.countDown()
        }
        Thread.sleep(1)
        executorService.execute() {
            pointService.chargePoint(userId, 1200L)
            countDownLatch.countDown()
        }
        Thread.sleep(1)
        executorService.execute() {
            pointService.usePoint(userId, 2000L)
            countDownLatch.countDown()
        }

        //then
        countDownLatch.await()

        val resultUserPoint = userPointTable.selectById(281L)
        assertThat(resultUserPoint.point).isEqualTo(0L)

        val resultPointHistories = pointHistoryTable.selectAllByUserId(281L)
        assertThat(resultPointHistories).hasSize(3)

        assertThat(resultPointHistories[0].amount).isEqualTo(800L)
        assertThat(resultPointHistories[0].type).isEqualTo(TransactionType.CHARGE)

        assertThat(resultPointHistories[1].amount).isEqualTo(1200L)
        assertThat(resultPointHistories[1].type).isEqualTo(TransactionType.CHARGE)

        assertThat(resultPointHistories[2].amount).isEqualTo(2000L)
        assertThat(resultPointHistories[2].type).isEqualTo(TransactionType.USE)
    }
}