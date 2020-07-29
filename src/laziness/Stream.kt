package laziness

import result.Result
import lists.List

sealed class Stream<out T> {

    abstract fun first(): Result<T>

    abstract fun rest(): Result<Stream<T>>

    abstract fun isEmpty(): Boolean

    abstract fun takeAtMost(n: Int): Stream<T>

    fun dropAtMost(n: Int): Stream<T> = dropAtMost(n, this)

    fun <T> repeat(f: () -> T): Stream<T> =
        Cons(Lazy { f() }, Lazy { repeat(f) })

    fun toList(): List<T> = toList(this)

    private object Empty : Stream<Nothing>() {
        override fun first(): Result<Nothing> = Result()

        override fun rest(): Result<Nothing> = Result()

        override fun isEmpty(): Boolean = true

        override fun takeAtMost(n: Int): Stream<Nothing> = this
    }

    private data class Cons<T>(val head: Lazy<T>, val tail: Lazy<Stream<T>>)
        : Stream<T>() {

        override fun first(): Result<T> = Result(head())

        override fun rest(): Result<Stream<T>> = Result(tail())

        override fun isEmpty(): Boolean = false

        override fun takeAtMost(n: Int): Stream<T> = when {
            n > 0 -> Cons(head, Lazy { tail().takeAtMost(n - 1) })
            else -> Empty
        }
    }

    companion object {
        operator
        fun <T> invoke(): Stream<T> = Empty

        fun <T> cons(head: Lazy<T>, tail: Lazy<Stream<T>>): Stream<T> =
            Cons(head, tail)

        tailrec fun <T> dropAtMost(n: Int, stream: Stream<T>): Stream<T> =
            if (stream is Cons && n > 0)
                dropAtMost(n - 1, stream.tail())
            else stream

        fun <T> toList(stream: Stream<T>): List<T> {
            tailrec fun <T> toList(stream: Stream<T>, list: List<T>): List<T> =
                when (stream) {
                    Empty -> list
                    is Cons -> toList(stream.tail(), list.cons(stream.head()))
                }

            return toList(stream, List()).reverse()
        }

        fun <T> iterate(seed: T, f: (T) -> T): Stream<T> =
            Cons(Lazy { seed }, Lazy { iterate(f(seed), f) })

        fun <T> iterate(seed: Lazy<T>, f: (T) -> T): Stream<T> =
            Cons(seed, Lazy { iterate(f(seed()), f) })

        fun from(i: Int): Stream<Int> = iterate(i) { it + 1 }
    }
}