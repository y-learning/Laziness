package laziness

import result.Result

sealed class Stream<out T> {

    abstract fun first(): Result<T>

    abstract fun rest(): Result<Stream<T>>

    abstract fun isEmpty(): Boolean

    fun <T> repeat(f: () -> T): Stream<T> =
        Cons(Lazy { f() }, Lazy { repeat(f) })

    private object Empty : Stream<Nothing>() {
        override fun first(): Result<Nothing> = Result()

        override fun rest(): Result<Nothing> = Result()

        override fun isEmpty(): Boolean = true
    }

    private data class Cons<T>(val head: Lazy<T>,
                               val tail: Lazy<Stream<T>>) : Stream<T>() {

        override fun first(): Result<T> = Result(head())

        override fun rest(): Result<Stream<T>> = Result(tail())

        override fun isEmpty(): Boolean = false
    }

    companion object {
        operator
        fun <T> invoke(): Stream<T> = Empty

        fun <T> cons(head: Lazy<T>, tail: Lazy<Stream<T>>): Stream<T> =
            Cons(head, tail)

        fun from(i: Int): Stream<Int> = Cons(Lazy { i }, Lazy { from(i + 1) })
    }
}