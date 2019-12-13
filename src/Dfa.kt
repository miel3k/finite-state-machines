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

    fun getCurrentStateInfo(): String {
        return if (finished) {
            "Finished state: $currentState"
        } else {
            "Current state: $currentState"
        }
    }
}