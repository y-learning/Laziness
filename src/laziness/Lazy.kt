package laziness

class Lazy<A>(function: () -> A) : () -> A {
    private val memoizedValue: A by lazy(function)

    override fun invoke(): A = memoizedValue

    companion object {
        fun <A, B, C> lift(f: (A) -> (B) -> C):
                (Lazy<A>) -> (Lazy<B>) -> Lazy<C> = { a ->
            { b ->
                Lazy { f(a())(b()) }
            }
        }
    }
}