class Nfa(
    private val transitionTable: Map<String, Map<String, List<String>>>,
    private val acceptStates: List<String>,
    val currentStates: MutableList<String>,
    val paths: MutableList<List<String>> = mutableListOf(),
    val terminatedPaths: MutableList<List<String>> = mutableListOf(),
    var finished: Boolean = false
) {

    fun processNextSymbol(symbol: String) {
        if (paths.isEmpty()) paths.add(currentStates)
        val updatedPaths = mutableListOf<List<String>>()
        val nextStates = currentStates.map { currentState ->
            getNextStates(symbol, currentState).also { nextStates ->
                paths.forEach { path ->
                    nextStates.forEach {
                        if (path.last() == currentState) {
                            if (isTerminatedState(it)) {
                                terminatedPaths.add(path + it)
                            } else {
                                updatedPaths.add(path + it)
                            }
                        }
                    }
                }
            }
        }.flatten()
        reinitializePaths(updatedPaths.toMutableList())
        reinitializeCurrentStates(nextStates.filterNotTerminated())
        if (nextStates.filterNotTerminated().isNullOrEmpty()) {
            finished = true
        }
    }

    private fun List<String>.filterNotTerminated() =
        filterNot { isTerminatedState(it) }

    private fun isTerminatedState(state: String) = state == TERMINATED_STATE

    private fun getNextStates(
        symbol: String,
        currentState: String
    ) = transitionTable[currentState]?.entries
        ?.find { it.key == symbol }
        ?.value
        .orEmpty()

    private fun reinitializePaths(newStatesHistory: MutableList<List<String>>) {
        paths.clear()
        paths.addAll(newStatesHistory)
    }

    private fun reinitializeCurrentStates(newCurrentStates: List<String>) {
        currentStates.clear()
        currentStates.addAll(newCurrentStates)
    }

    fun getState() = when {
        !finished -> State.Working
        !acceptStates.any { currentStates.contains(it) } -> State.Failure
        isRepetitionInBoth() -> State.Success.RepetitionInBoth
        isRepetitionInNumbers() -> State.Success.RepetitionInNumbers
        isRepetitionInLetters() -> State.Success.RepetitionInLetters
        else -> State.Undefined
    }

    private fun isRepetitionInBoth() =
        isRepetitionInNumbers() && isRepetitionInLetters()

    private fun isRepetitionInNumbers() = currentStates.any {
        it.contains("00") ||
                it.contains("11") ||
                it.contains("22") ||
                it.contains("33") ||
                it.contains("44")
    }

    private fun isRepetitionInLetters() = currentStates.any {
        it.contains("aa") ||
                it.contains("bb") ||
                it.contains("cc") ||
                it.contains("dd") ||
                it.contains("ee")
    }

    sealed class State {
        object Undefined : State()
        object Working : State()
        object Failure : State()
        sealed class Success : State() {
            object RepetitionInNumbers : Success()
            object RepetitionInLetters : Success()
            object RepetitionInBoth : Success()
        }
    }

    companion object {
        const val TERMINATED_STATE = "X"
    }
}