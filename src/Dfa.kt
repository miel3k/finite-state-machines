class Dfa(
    private val transitionTable: Map<String, Map<String, String>>,
    private val acceptStates: List<String>,
    private var currentState: String,
    var finished: Boolean = false
) {

    fun processNextSymbol(symbol: String) {
        val nextState = transitionTable[currentState]?.entries
            ?.find { it.key == symbol }
            ?.value
        nextState?.let { currentState = it }
        if (acceptStates.contains(currentState)) {
            finished = true
        }
    }

    fun getState() = if (finished) {
        State.Finished(currentState)
    } else {
        State.Working(currentState)
    }

    sealed class State(val value: String) {
        class Working(value: String) : State(value)
        class Finished(value: String) : State(value)
    }
}