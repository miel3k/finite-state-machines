import java.io.File

class Nfa(
    private val transitionTable: Map<String, Map<String, List<String>>>,
    val currentStates: MutableList<String>,
    val statesHistory: MutableList<String> = mutableListOf()
) {

    fun processNextSymbol(symbol: String) {
        val nextStates = currentStates.mapNotNull { currentState ->
            transitionTable[currentState]?.entries
                ?.find { it.key == symbol }
                ?.value
        }.flatten()
        statesHistory.addAll(currentStates)
        currentStates.clear()
        currentStates.addAll(nextStates)
    }
}

fun main() {
    val entriesStream = File("nfa_data/entries.txt").inputStream()
    val entries = entriesStream.bufferedReader().use {
        it.readText().split("#")
    }
    val alphabetStream = File("nfa_data/alphabet.txt").inputStream()
    val alphabet = alphabetStream.bufferedReader().use {
        it.readText().split(",")
    }
    val transitionTableStream = File("nfa_data/tt.txt").inputStream()
    val transitionTable = mutableMapOf<String, Map<String, List<String>>>()
    transitionTableStream.bufferedReader().useLines { lines ->
        lines.forEach {
            val states = it.split(",")
            val inState = states.first()
            val transitionMap = states.drop(1).mapIndexed { index, outState ->
                if (outState.contains("#")) {
                    val multiStates = outState.split("#")
                    Pair(alphabet[index], multiStates)
                } else {
                    Pair(alphabet[index], listOf(outState))
                }
            }.toMap()
            transitionTable[inState] = transitionMap
        }
    }
    val acceptStatesStream = File("nfa_data/accept_states.txt").inputStream()
    val acceptStates = mutableListOf<String>()
    acceptStatesStream.bufferedReader().useLines { lines ->
        lines.forEach { acceptStates.add(it) }
    }
    val startState = transitionTable.keys.first()
    entries.forEach { entry ->
        val nfa = Nfa(transitionTable, listOf(startState).toMutableList())
        entry.chunked(1).forEach { symbol ->
            println("Current state(s): ${nfa.currentStates}")
            nfa.processNextSymbol(symbol)
        }
        println("Current state(s): ${nfa.currentStates}")
        if (acceptStates.any { nfa.currentStates.contains(it) }) {
            when {
                nfa.currentStates.any { it.contains("2C2L") || it.contains("2L2C") } -> {
                    println("Repetition occurred in both numbers and letters")
                }
                nfa.currentStates.any { it.contains("2C") } -> {
                    println("Repetition occurred in numbers")
                }
                else -> {
                    println("Repetition occurred in letters")
                }
            }
        }
        println("States history: ${nfa.statesHistory}")
        println("Type anything to process next series")
        readLine()
    }
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