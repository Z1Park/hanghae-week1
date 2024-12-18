package io.hhplus.tdd.point

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class PointValidatorTest {

    private val pointValidator = PointValidator()

    @Test
    fun `충전 포인트가 음수면 충전이 불가능하다`() {
        // given

        // when then
        assertThatThrownBy { pointValidator.validateChargeable(45, -1) }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage("0 또는 음수 포인트는 충전할 수 없습니다. 충전 포인트=-1")
    }

    @Test
    fun `충전 포인트가 한도를 초과하면 충전이 불가능하다`() {
        // given

        // when then
        assertThatThrownBy { pointValidator.validateChargeable(1, 1_000_000) }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage("한도를 초과하여 충전할 수 없습니다. 포인트=1, 충전 포인트=1000000, 한도 포인트=1000000")
    }

    @Test
    fun `충전 포인트가 한도를 초과하면 충전이 불가능하다 - overflow`() {
        // given

        // when then
        assertThatThrownBy { pointValidator.validateChargeable(1, Long.MAX_VALUE) }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage("한도를 초과하여 충전할 수 없습니다. 포인트=1, 충전 포인트=9223372036854775807, 한도 포인트=1000000")
    }
}