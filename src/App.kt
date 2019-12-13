import java.io.File

fun main() {
    runDfa()
}

fun runDfa() {
    val alphabetStream = File("dfa_data/alphabet.txt").inputStream()
    val alphabet = alphabetStream.bufferedReader().use {
        it.readText().split(",")
    }
    val transitionTableStream = File("dfa_data/tt.txt").inputStream()
    val transitionTable = mutableMapOf<String, Map<String, String>>()
    transitionTableStream.bufferedReader().useLines { lines ->
        lines.forEach {
            val states = it.split(",")
            val inState = states.first()
            val transitionMap = states.drop(1).mapIndexed { index, outState ->
                Pair(alphabet[index], outState)
            }.toMap()
            transitionTable[inState] = transitionMap
        }
    }
    val acceptStatesStream = File("dfa_data/accept_states.txt").inputStream()
    val acceptStates = mutableListOf<String>()
    acceptStatesStream.bufferedReader().useLines { lines ->
        lines.forEach { acceptStates.add(it) }
    }
    val startState = transitionTable.keys.first()
    val dfa = Dfa(transitionTable, acceptStates, startState)
    println("Alphabet = { " + alphabet.joinToString(",") + "}")
    while (!dfa.finished) {
        println("Enter symbol from alphabet:")
        var input = readLine()
        while (!alphabet.contains(input)) {
            println("Wrong input, try again:")
            input = readLine()
        }
        input?.let {
            dfa.processNextSymbol(it.toLowerCase())
            println(dfa.getCurrentStateInfo())
        }
    }
}