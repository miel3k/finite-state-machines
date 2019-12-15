class Nfa(
    private val transitionTable: Map<String, Map<String, List<String>>>,
    private val acceptStates: List<String>,
    val currentStates: MutableList<String>,
    val paths: MutableList<List<String>> = mutableListOf(),
    var finished: Boolean = false
) {

    fun processNextSymbol(symbol: String) {
        if (paths.isEmpty()) paths.add(currentStates)
        val pathsCopy = paths.toMutableList()
        val updatedPaths = mutableListOf<List<String>>()
        val nextStates = currentStates.map { currentState ->
            getNextStates(symbol, currentState).also {
                it.forEach { nextState ->
                    pathsCopy.filter { states ->
                        states.last() == currentState
                    }.forEach { states ->
                        updatedPaths.add((states + nextState).toMutableList())
                    }
                }
                val terminatedPaths = pathsCopy.filterTerminatedPaths()
                updatedPaths.addAll(terminatedPaths)
            }
        }.flatten()
        reinitializePaths(updatedPaths)
        reinitializeCurrentStates(nextStates)
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

    private fun MutableList<List<String>>.filterTerminatedPaths(): MutableList<List<String>> {
        return filter { isTerminatedState(it.last()) }.toMutableList()
    }

    fun getState() = when {
        !finished -> State.Working
        !acceptStates.any { currentStates.contains(it) } -> State.Failure
        currentStates.any { it.contains("2C2L") || it.contains("2L2C") } ->
            State.Success.RepetitionInBoth
        currentStates.any { it.contains("2C") } ->
            State.Success.RepetitionInNumbers
        currentStates.any { it.contains("2L") } ->
            State.Success.RepetitionInLetters
        else -> State.Undefined
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