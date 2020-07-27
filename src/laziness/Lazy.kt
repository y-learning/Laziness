package laziness

import lists.List

class Lazy<A>(function: () -> A) : () -> A {
    private val memoizedValue: A by lazy(function)

    override fun invoke(): A = memoizedValue

    fun <B> map(f: (A) -> B): Lazy<B> = Lazy { f(memoizedValue) }

    fun <B> flatMap(f: (A) -> Lazy<B>): Lazy<B> = Lazy { f(memoizedValue)() }

    companion object {
        fun <A, B, C> lift(f: (A) -> (B) -> C):
            (Lazy<A>) -> (Lazy<B>) -> Lazy<C> = { a ->
            { b ->
                Lazy { f(a())(b()) }
            }
        }
    }
}

fun <A> sequence(list: List<Lazy<A>>): Lazy<List<A>> =
    Lazy { list.map { it() } }