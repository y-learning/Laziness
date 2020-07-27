import laziness.Lazy
import java.lang.IllegalStateException
import java.util.*
import kotlin.random.Random

fun or(a: Lazy<Boolean>, b: Lazy<Boolean>): Boolean = if (a()) true else b()

fun constructMessage(greetings: Lazy<String>, name: Lazy<String>):
        Lazy<String> = Lazy { "${greetings()} ${name()}!" }

val constructMessage: (Lazy<String>) -> (Lazy<String>) -> Lazy<String> =
        { greetings ->
            { name ->
                Lazy { "${greetings()} ${name()}!" }
            }
        }

val consMessage: (String) -> (String) -> String = { greetings ->
    { name -> "$greetings $name!" }
}

fun getGreetings(us: Locale?): String {
    println("Evaluating greetings")
    return "Hello"
}

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

    val name3 = Lazy {
        println("Evaluating name3")
        "Minnie"
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

    val greetingHello = constructMessage(greetings)
    val message3 = greetingHello(name3)
    println(if (condition) message3() else defaultMessage())
    println(if (condition) message3() else defaultMessage())

    val message4 = Lazy.lift(consMessage)(greetings)(name1)
    println(if (condition) message4() else defaultMessage())

    // ---------------------
    println()

    val greets: (String) -> String = { "Hello, $it" }
    val msg = name1.map(greets)
    println(if (condition) msg() else defaultMessage())
    println(if (condition) msg() else defaultMessage())

    // ---------------------
    println()

    val greetings2: Lazy<String> = Lazy { getGreetings(Locale.US) }
    val flatGreets: (String) -> Lazy<String> = { name ->
        println("okokokok")
        greetings2.map { "$it, $name!" }
    }

    val name: Lazy<String> = Lazy {
        println("computing name")
        "why?"
    }

    val defaultMsg: Lazy<String> = Lazy {
        println("Evaluating default msg")
        "No greetings"
    }

    val message = name.flatMap(flatGreets)

    println(if (condition) message() else defaultMsg())
    println(if (condition) message() else defaultMsg())
}
