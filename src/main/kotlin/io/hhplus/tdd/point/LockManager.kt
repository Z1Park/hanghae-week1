package io.hhplus.tdd.point

import org.springframework.stereotype.Component
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

@Component
class LockManager {

    private val locks = ConcurrentHashMap<Long, ReentrantReadWriteLock>()

    fun <T> read(id: Long, callable: Callable<T>): T {
        val userLock = getLock(id)

        return userLock.read { callable.call() }
    }

    fun <T> write(id: Long, callable: Callable<T>): T {
        val userLock = getLock(id)

        return userLock.write { callable.call() }
    }

    private fun getLock(id: Long): ReentrantReadWriteLock {
        synchronized(this) {
            if (!locks.containsKey(id)) {
                locks[id] = ReentrantReadWriteLock(true)
            }
        }
        return locks[id]!!
    }
}