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
    private val pointLockManager = LockManager()
    private val historyLockManager = LockManager()

    fun getUserPoint(id: Long): UserPoint {
        return pointLockManager.read(id) { userPointTable.selectById(id) }
    }

    fun getUserPointHistory(id: Long): List<PointHistory> {
        return historyLockManager.read(id) { pointHistoryTable.selectAllByUserId(id) }
    }

    fun chargePoint(id: Long, amount: Long): UserPoint {
        val updatedUserPoint = pointLockManager.write(id) {
            val userPoint = userPointTable.selectById(id)

            pointValidator.validateChargeable(userPoint.point, amount)
            userPointTable.insertOrUpdate(id, userPoint.point + amount)
        }

        historyLockManager.write(id) {
            pointHistoryTable.insert(id, amount, TransactionType.CHARGE, updatedUserPoint.updateMillis)
        }

        return updatedUserPoint
    }

    fun usePoint(id: Long, amount: Long): UserPoint {
        val usedUserPoint = pointLockManager.write(id) {
            val userPoint = userPointTable.selectById(id)

            pointValidator.validateUseable(userPoint.point, amount)

            val remainPoint = userPoint.point - amount
            userPointTable.insertOrUpdate(id, remainPoint)
        }

        historyLockManager.write(id) {
            pointHistoryTable.insert(id, amount, TransactionType.USE, usedUserPoint.updateMillis)
        }

        return usedUserPoint
    }
}