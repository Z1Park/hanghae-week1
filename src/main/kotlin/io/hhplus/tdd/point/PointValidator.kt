package io.hhplus.tdd.point

import org.springframework.stereotype.Component

@Component
class PointValidator {

    companion object {
        private const val CHARGE_LIMIT = 1_000_000
    }

    fun validateChargeable(point: Long, amount: Long) {
        if (amount <= 0) throw RuntimeException("0 또는 음수 포인트는 충전할 수 없습니다. 충전 포인트=${amount}")

        val chargePoint = point + amount
        if (chargePoint > CHARGE_LIMIT || chargePoint < 0)
            throw RuntimeException("한도를 초과하여 충전할 수 없습니다. 포인트=${point}, 충전 포인트=${amount}, 한도 포인트=${CHARGE_LIMIT}")
    }
}