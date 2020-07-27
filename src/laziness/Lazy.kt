package laziness

class Lazy<A>(function: () -> A) : () -> A {
    private val memoizedValue: A by lazy(function)

    override fun invoke(): A = memoizedValue
}