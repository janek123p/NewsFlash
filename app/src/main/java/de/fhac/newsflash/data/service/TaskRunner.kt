package de.fhac.newsflash.data.service

import java.util.concurrent.*

object TaskRunner {
    private var THREAD_POOL_EXECUTOR: Executor = ThreadPoolExecutor(10, 128, 1, TimeUnit.SECONDS, LinkedBlockingQueue<Runnable>());

    interface CallBack<R>{
        fun onComplete(result: R);
    }

    fun <R> executeAsync(callable: Callable<R>, callBack: CallBack<R>){
        THREAD_POOL_EXECUTOR.execute {
            var result = callable.call();
            
            callBack.onComplete(result);
        }
    }
}