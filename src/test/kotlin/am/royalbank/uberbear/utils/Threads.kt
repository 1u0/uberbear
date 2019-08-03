package am.royalbank.uberbear.utils

import java.util.concurrent.CyclicBarrier
import kotlin.concurrent.thread

object Threads {

    fun runSimultaneously(count: Int, runnable: () -> Unit): Array<Thread> {
        val barrier = CyclicBarrier(count + 1)
        val threads = (1..count)
            .map {
                thread {
                    barrier.await()
                    runnable.invoke()
                }
            }
            .toTypedArray()
        barrier.await()
        return threads
    }

    fun awaitAll(threads: Array<Thread>) {
        for (thread in threads) {
            thread.join()
        }
    }
}
