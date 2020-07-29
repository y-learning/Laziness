package laziness

import result.Result
import lists.List

sealed class Stream<out T> {

    abstract fun first(): Result<T>

    abstract fun rest(): Result<Stream<T>>

    abstract fun isEmpty(): Boolean

    abstract fun takeAtMost(n: Int): Stream<T>

    abstract fun takeWhile(p: (T) -> Boolean): Stream<T>

    abstract fun <U> foldRight(acc: Lazy<U>, f: (T) -> (Lazy<U>) -> U): U

    fun dropAtMost(n: Int): Stream<T> = dropAtMost(n, this)

    fun dropWhile(p: (T) -> Boolean): Stream<T> = dropWhile(this, p)

    fun <T> repeat(f: () -> T): Stream<T> =
        Cons(Lazy { f() }, Lazy { repeat(f) })

    fun toList(): List<T> = toList(this)

    fun exists(p: (T) -> Boolean): Boolean = exists(this, p)

    fun takeWhileViaFoldRight(p: (T) -> Boolean): Stream<T> =
        foldRight(Lazy<Stream<T>> { Empty }) { item: T ->
            { lStream: Lazy<Stream<T>> ->
                if (p(item))
                    Cons(Lazy { item }, lStream)
                else
                    lStream()
            }
        }

    fun firstSafe(): Result<T> =
        foldRight(Lazy { Result<T>() }) { t: T -> { Result(t) } }

    fun <U> map(f: (T) -> U): Stream<U> = foldRight(Lazy<Stream<U>> { Empty })
    { e: T ->
        { acc: Lazy<Stream<U>> ->
            Cons(Lazy { f(e) }, acc)
        }
    }

    private object Empty : Stream<Nothing>() {
        override fun first(): Result<Nothing> = Result()

        override fun rest(): Result<Nothing> = Result()

        override fun isEmpty(): Boolean = true

        override fun takeAtMost(n: Int): Stream<Nothing> = this

        override fun takeWhile(p: (Nothing) -> Boolean): Stream<Nothing> = this

        override fun <U> foldRight(acc: Lazy<U>,
                                   f: (Nothing) -> (Lazy<U>) -> U): U = acc()
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

        override fun takeWhile(p: (T) -> Boolean): Stream<T> = when {
            p(head()) -> Cons(head, Lazy { tail().takeWhile(p) })
            else -> Empty
        }

        override fun <U> foldRight(acc: Lazy<U>, f: (T) -> (Lazy<U>) -> U): U =
            f(head())(Lazy { tail().foldRight(acc, f) })
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

        tailrec fun <T> dropWhile(stream: Stream<T>,
                                  p: (T) -> Boolean): Stream<T> =
            if (stream is Cons && p(stream.head()))
                dropWhile(stream.tail(), p)
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

        tailrec fun <T> exists(stream: Stream<T>, p: (T) -> Boolean): Boolean =
            when (stream) {
                Empty -> false
                is Cons -> when {
                    p(stream.head()) -> true
                    else -> exists(stream.tail(), p)
                }
            }
    }
}