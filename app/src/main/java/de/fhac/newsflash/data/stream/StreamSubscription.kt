package de.fhac.newsflash.data.stream

abstract class StreamSubscription<T>(private val callback: (T?) -> Unit) {
    private var isPaused = false;

    private fun notifyChange(value: T?) {
        if (!isPaused)
            callback(value);
    }

    fun pause() {
        this.isPaused = true;
    }

    fun resume() {
        this.isPaused = false;
    }

    abstract fun cancel(): Boolean;

    class Stream<T> {
        private var value: T? = null;

        private val subscriptions: MutableList<StreamSubscription<T>> = mutableListOf();

        fun listen(callback: (T?) -> Unit, pushLatest: Boolean = false): StreamSubscription<T> {
            val subscription = object : StreamSubscription<T>(callback) {
                override fun cancel() = subscriptions.remove(this as StreamSubscription<T>);
            } as StreamSubscription<T>;

            subscriptions.add(subscription)

            if (pushLatest)
                callback(value)

            return subscription;
        }

        fun getLatest() = value;


        private fun update(value: T?) {
            this.value = value;

            for (sub in subscriptions)
                sub.notifyChange(value);
        }

        class StreamController<T> {
            private val stream: Stream<T> = Stream();

            fun getStream() = stream;
            fun getSink(): Sink<T> {
                return Sink<T>(this);
            }

            private fun add(event: T?) {
                stream.update(event);
            }

            class Sink<T>(private val streamController: StreamController<T>) {

                fun add(value: T?) {
                    streamController.add(value);
                }
            }

        }
    }
}



