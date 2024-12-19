package io.hhplus.tdd.point

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class LockManagerUnitTest {

    private val lockManager = LockManager()

    /**
     * 동일한 key로 Read 경쟁 테스트
     */
    @Test
    fun `동일한 id로 100ms 소요되는 3개의 read가 발생하면 전체 소요시간은 120ms보다 적어야한다`() {
        // given
        val id = 401L
        val requestCount = 3

        val countDownLatch = CountDownLatch(requestCount)
        val executorService = Executors.newFixedThreadPool(requestCount)
        val startMillis = System.currentTimeMillis()

        // when
        for (i in 1..requestCount) {
            executorService.execute() {
                lockManager.read(id) {
                    Thread.sleep(100)
                }
                countDownLatch.countDown()
            }
        }

        //then
        countDownLatch.await()

        val endMillis = System.currentTimeMillis()
        assertThat(endMillis - startMillis).isLessThan(120)
    }

    /**
     * 동일한 key로 Write 경쟁 테스트
     */
    @Test
    fun `동일한 id로 100ms 소요되는 3개의 write가 발생하면 전체 소요시간은 300ms보다 커야한다`() {
        // given
        val id = 411L
        val requestCount = 3

        val countDownLatch = CountDownLatch(requestCount)
        val executorService = Executors.newFixedThreadPool(requestCount)
        val startMillis = System.currentTimeMillis()

        // when
        for (i in 1..requestCount) {
            executorService.execute() {
                lockManager.write(id) {
                    Thread.sleep(100)
                }
                countDownLatch.countDown()
            }
        }

        //then
        countDownLatch.await()

        val endMillis = System.currentTimeMillis()
        assertThat(endMillis - startMillis).isGreaterThan(300)
    }

    /**
     * 서로 다른 key로 Write 경쟁 테스트
     */
    @Test
    fun `서로 다른 id 3개로 100ms 소요되는 write가 발생하면 전체 소요시간은 120ms보다 적어야한다`() {
        // given
        var id = 421L
        val requestCount = 3

        val countDownLatch = CountDownLatch(requestCount)
        val executorService = Executors.newFixedThreadPool(requestCount)
        val startMillis = System.currentTimeMillis()

        // when
        for (i in 1..requestCount) {
            val currentId = id + i
            executorService.execute() {
                lockManager.write(currentId) {
                    Thread.sleep(100)
                }
                countDownLatch.countDown()
            }
        }

        //then
        countDownLatch.await()

        val endMillis = System.currentTimeMillis()
        assertThat(endMillis - startMillis).isLessThan(120)
    }

    /**
     * 동일한 key로 read + Write 경쟁 테스트
     */
    @Test
    fun `동일한 id로 100ms 소요되는 read가 3개 200ms 소요되는 write가 2개 순차적으로 발생하면 전체 소요시간은 500ms보다 크고 520ms보다 적어야한다`() {
        // given
        val id = 431L
        val requestCount = 5

        val countDownLatch = CountDownLatch(requestCount)
        val executorService = Executors.newFixedThreadPool(requestCount)
        val startMillis = System.currentTimeMillis()

        // when
        for (i in 1..3) {
            executorService.execute() {
                lockManager.read(id) {
                    Thread.sleep(100)
                }
                countDownLatch.countDown()
            }
        }
        Thread.sleep(1)

        for (i in 1..2) {
            executorService.execute() {
                lockManager.write(id) {
                    Thread.sleep(200)
                }
                countDownLatch.countDown()
            }
        }

        //then
        countDownLatch.await()

        val endMillis = System.currentTimeMillis()
        assertThat(endMillis - startMillis)
            .isGreaterThan(500)
            .isLessThan(520)
    }
}