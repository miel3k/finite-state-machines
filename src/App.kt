import java.io.File

fun main() {
    println("Choose finite state machine: DFA('D'), NFA('N')  ")
    var fsmType = readLine()
    while (fsmType != "D" && fsmType != "N") {
        println("Wrong input, try again:")
        fsmType = readLine()
    }
    when (fsmType) {
        "D" -> runDfa()
        "N" -> runNfa()
    }
}

fun runNfa() {
    val entries = readTextFromFile("nfa_data/entries.txt")
    val alphabet = readTextFromFile("nfa_data/alphabet.txt")
    val transitionTable = readNfaTransitionTableFromFile("nfa_data/tt.txt", alphabet)
    val acceptStates = readTextFromFile("nfa_data/accept_states.txt")
    val startState = transitionTable.keys.first()
    displayAlphabet(alphabet)
    entries.forEach { entry ->
        println("Series: $entry")
        val nfa = Nfa(transitionTable, acceptStates, mutableListOf(startState))
        entry.chunked(1).forEach { symbol ->
            if (nfa.finished) return@forEach
            println("Current state(s): ${nfa.currentStates}")
            nfa.processNextSymbol(symbol)
        }
        nfa.finished = true
        println("Current state(s): ${nfa.currentStates}")
        val stateMessage = when (nfa.getState()) {
            is Nfa.State.Working -> "Working"
            is Nfa.State.Failure -> "Series not accepted by nfa"
            is Nfa.State.Success.RepetitionInBoth ->
                "Repetition occurred in both numbers and letters"
            is Nfa.State.Success.RepetitionInNumbers ->
                "Repetition occurred in numbers"
            is Nfa.State.Success.RepetitionInLetters ->
                "Repetition occurred in letters"
            Nfa.State.Undefined -> "Undefined state"
        }
        println(stateMessage)
        (nfa.terminatedPaths + nfa.paths).forEach { displayPath(it) }
        println("Type 'p' to process next series")
        readLine()
    }
    println("All series processed")
}

fun runDfa() {
    val alphabet = readTextFromFile("dfa_data/alphabet.txt")
    val transitionTable = readDfaTransitionTableFromFile("dfa_data/tt.txt", alphabet)
    val acceptStates = readTextFromFile("dfa_data/accept_states.txt", "\n")
    val startState = transitionTable.keys.first()
    val dfa = Dfa(transitionTable, acceptStates, startState)
    displayAlphabet(alphabet)
    while (!dfa.finished) {
        println("Enter symbol from alphabet:")
        var input = readLine()
        while (!alphabet.contains(input)) {
            println("Wrong input, try again:")
            input = readLine()
        }
        input?.let {
            dfa.processNextSymbol(it.toLowerCase())
            val stateMessage = when (val dfaState = dfa.getState()) {
                is Dfa.State.Working -> "Current state: ${dfaState.value}"
                is Dfa.State.Finished -> "Finished state: ${dfaState.value}"
            }
            println(stateMessage)
        }
    }
    displayPath(dfa.path)
}

fun displayPath(path: List<String>) {
    println(path.joinToString(" -> "))
}

fun displayAlphabet(alphabet: List<String>) {
    println("Σ = { " + alphabet.joinToString(",") + " }")
}

fun readTextFromFile(path: String, delimiter: String = "#") =
    File(path).inputStream().bufferedReader().use {
        it.readText().split(delimiter)
    }

fun readNfaTransitionTableFromFile(
    path: String,
    alphabet: List<String>,
    stateDelimiter: String = ",",
    statesDelimiter: String = "#"
): Map<String, Map<String, List<String>>> {
    val transitionTable = mutableMapOf<String, Map<String, List<String>>>()
    File(path).inputStream().bufferedReader().useLines { lines ->
        lines.forEach {
            val states = it.split(stateDelimiter)
            val inState = states.first()
            val transitionMap = states.drop(1).mapIndexed { index, outState ->
                if (outState.contains(statesDelimiter)) {
                    val multiStates = outState.split(statesDelimiter)
                    Pair(alphabet[index], multiStates)
                } else {
                    Pair(alphabet[index], listOf(outState))
                }
            }.toMap()
            transitionTable[inState] = transitionMap
        }
    }
    return transitionTable
}

fun readDfaTransitionTableFromFile(
    path: String,
    alphabet: List<String>,
    stateDelimiter: String = ","
): Map<String, Map<String, String>> {
    val transitionTable = mutableMapOf<String, Map<String, String>>()
    File(path).inputStream().bufferedReader().useLines { lines ->
        lines.forEach {
            val states = it.split(stateDelimiter)
            val inState = states.first()
            val transitionMap = states.drop(1).mapIndexed { index, outState ->
                Pair(alphabet[index], outState)
            }.toMap()
            transitionTable[inState] = transitionMap
        }
    }
    return transitionTable
}