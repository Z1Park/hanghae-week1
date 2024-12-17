package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PointService(
    private val userPointTable: UserPointTable,
    private val pointHistoryTable: PointHistoryTable
) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)
}