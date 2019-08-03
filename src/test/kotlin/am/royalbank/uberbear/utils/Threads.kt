package am.royalbank.uberbear.utils

import java.util.concurrent.Callable
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.Future

object Threads {

    fun <R> runInParallel(parallelism: Int, runnable: () -> R): List<R> {
        val threadPool = Executors.newFixedThreadPool(parallelism)
        val barrier = CyclicBarrier(parallelism)
        val callable = Callable {
            barrier.await()
            runnable.invoke()
        }
        val results = (1..parallelism)
            .map { threadPool.submit(callable) }
        return results.map(Future<R>::get)
    }
}
