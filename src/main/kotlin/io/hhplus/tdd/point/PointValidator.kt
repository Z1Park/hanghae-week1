package io.hhplus.tdd.point

import org.apache.coyote.BadRequestException
import org.springframework.stereotype.Component

@Component
class PointValidator {

    companion object {
        private const val CHARGE_LIMIT = 1_000_000
    }

    @Throws(BadRequestException::class)
    fun validateChargeable(point: Long, amount: Long) {
        if (amount <= 0) throw BadRequestException("0 또는 음수 포인트는 충전할 수 없습니다. 충전 포인트=${amount}")

        val chargePoint = point + amount
        if (chargePoint > CHARGE_LIMIT || chargePoint < 0)
            throw BadRequestException("한도를 초과하여 충전할 수 없습니다. 포인트=${point}, 충전 포인트=${amount}, 한도 포인트=${CHARGE_LIMIT}")
    }

    @Throws(BadRequestException::class)
    fun validateUseable(point: Long, amount: Long) {
        if (amount <= 0) throw BadRequestException("0 또는 음수 포인트는 사용할 수 없습니다. 사용 포인트=${amount}")
        if (point < amount) throw BadRequestException("잔고가 부족하여 사용할 수 없습니다. 잔고 포인트=${point}, 사용 포인트=${amount}")
    }
}