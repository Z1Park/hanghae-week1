package io.hhplus.tdd.point

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PointValidatorTest {

    private val pointValidator = PointValidator()

    @Nested
    inner class `충전` {
        @Test
        fun `충전 포인트가 0이면 충전이 불가능하다`() {
            // given
            val point = 45L
            val amount = 0L

            // when then
            assertThatThrownBy { pointValidator.validateChargeable(point, amount) }
                .isInstanceOf(RuntimeException::class.java)
                .hasMessage("0 또는 음수 포인트는 충전할 수 없습니다. 충전 포인트=0")
        }

        @Test
        fun `충전 포인트가 음수면 충전이 불가능하다`() {
            // given
            val point = 45L
            val amount = -1L

            // when then
            assertThatThrownBy { pointValidator.validateChargeable(point, amount) }
                .isInstanceOf(RuntimeException::class.java)
                .hasMessage("0 또는 음수 포인트는 충전할 수 없습니다. 충전 포인트=-1")
        }

        @Test
        fun `충전 포인트가 한도를 초과하면 충전이 불가능하다`() {
            // given
            val point = 1L
            val amount = 1_000_000L

            // when then
            assertThatThrownBy { pointValidator.validateChargeable(point, amount) }
                .isInstanceOf(RuntimeException::class.java)
                .hasMessage("한도를 초과하여 충전할 수 없습니다. 포인트=1, 충전 포인트=1000000, 한도 포인트=1000000")
        }

        @Test
        fun `충전 포인트가 한도를 초과하면 충전이 불가능하다 - overflow`() {
            // given
            val point = 1L
            val amount = Long.MAX_VALUE

            // when then
            assertThatThrownBy { pointValidator.validateChargeable(point, amount) }
                .isInstanceOf(RuntimeException::class.java)
                .hasMessage("한도를 초과하여 충전할 수 없습니다. 포인트=1, 충전 포인트=9223372036854775807, 한도 포인트=1000000")
        }
    }

    @Nested
    inner class `사용` {
        @Test
        fun `사용 포인트가 0이면 사용이 불가능하다`() {
            // given
            val point = 100L
            val amount = 0L

            // when then
            assertThatThrownBy { pointValidator.validateUseable(point, amount) }
                .isInstanceOf(RuntimeException::class.java)
                .hasMessage("0 또는 음수 포인트는 사용할 수 없습니다. 사용 포인트=0")
        }

        @Test
        fun `사용 포인트가 음수면 사용이 불가능하다`() {
            // given
            val point = 100L
            val amount = -1L

            // when then
            assertThatThrownBy { pointValidator.validateUseable(point, amount) }
                .isInstanceOf(RuntimeException::class.java)
                .hasMessage("0 또는 음수 포인트는 사용할 수 없습니다. 사용 포인트=-1")
        }

        @Test
        fun `잔고보다 사용하려는 포인트가 많으면 사용할 수 없다`() {
            // given
            val point = 700L
            val amount = 701L

            // when then
            assertThatThrownBy { pointValidator.validateUseable(point, amount) }
                .isInstanceOf(RuntimeException::class.java)
                .hasMessage("잔고가 부족하여 사용할 수 없습니다. 잔고 포인트=700, 사용 포인트=701")
        }
    }
}