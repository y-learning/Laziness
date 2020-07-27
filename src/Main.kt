import java.lang.IllegalStateException

fun main() {
    val first = Lazy {
        println("Evaluating first")
        true
    }

    val second = Lazy<Boolean> {
        println("Evaluating second")
        throw IllegalStateException()
    }

    println(first() || second())
    println(first() || second())
    println(or(first, second))
}

fun or(a: Lazy<Boolean>, b: Lazy<Boolean>): Boolean = if (a()) true else b()