import laziness.Lazy
import java.lang.IllegalStateException
import kotlin.random.Random

fun or(a: Lazy<Boolean>, b: Lazy<Boolean>): Boolean = if (a()) true else b()

fun constructMessage(greetings: Lazy<String>, name: Lazy<String>):
        Lazy<String> = Lazy { "${greetings()} ${name()}!" }

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

    //------------------------------------
    println()

    val greetings = Lazy {
        println("Evaluating greetings")
        "Hello"
    }

    val name1 = Lazy {
        println("Evaluating name1")
        "Mickey"
    }

    val name2 = Lazy {
        println("Evaluating name2")
        "Donald"
    }

    val defaultMessage = Lazy {
        println("Evaluating default message")
        "No greetings when time is odd"
    }

    val message1 = constructMessage(greetings, name1)
    val message2 = constructMessage(greetings, name2)
    val condition = Random(System.currentTimeMillis()).nextInt() % 2 == 0

    println(if (condition) message1() else defaultMessage())
    println(if (condition) message1() else defaultMessage())
    println(if (condition) message2() else defaultMessage())
}
