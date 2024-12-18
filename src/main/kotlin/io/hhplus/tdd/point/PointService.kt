package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PointService(
    private val userPointTable: UserPointTable,
    private val pointHistoryTable: PointHistoryTable,
    private val pointValidator: PointValidator
) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val LOG_MESSAGE_FORMAT = "call {}: {}"
    }

    private fun logging(method: String, param: String) = logger.info(LOG_MESSAGE_FORMAT, method, param)

    fun getUserPoint(id: Long): UserPoint {
        logging("getUserPoint", "id=${id}")

        return userPointTable.selectById(id)
    }

    fun getUserPointHistory(id: Long): List<PointHistory> {
        logging("getUserPointHistory", "id=${id}");

        return pointHistoryTable.selectAllByUserId(id)
    }

    fun chargePoint(id: Long, amount: Long): UserPoint {
        logging("chargePoint", "id=${id}");

        val userPoint = userPointTable.selectById(id)

        pointValidator.validateChargeable(userPoint.point, amount)
        val updatedUserPoint = userPointTable.insertOrUpdate(id, userPoint.point + amount)

        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, updatedUserPoint.updateMillis)

        return updatedUserPoint
    }
}