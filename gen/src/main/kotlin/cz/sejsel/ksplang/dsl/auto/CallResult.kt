package cz.sejsel.ksplang.dsl.auto

import cz.sejsel.ksplang.dsl.core.KsplangMarker

@KsplangMarker
sealed interface CallResult {
    fun clear()
}


@KsplangMarker
class EmptyResult : CallResult {
    override fun clear() {}
}

@KsplangMarker
class CallResult1(private val callProcessor: CallResultProcessor) : CallResult {
    fun setTo(v: Variable) {
        callProcessor.setTo(v, 0)
    }

    override fun clear() {
        callProcessor.clearResults()
    }
}

@KsplangMarker
class CallResult2(private val callProcessor: CallResultProcessor) : CallResult {
    fun setTo(var1: Variable, var2: Variable) {
        callProcessor.setTo(var1, 0)
        callProcessor.setTo(var2, 1)
    }

    fun setFirstTo(variable: Variable) {
        callProcessor.setTo(variable, 0)
    }

    fun setSecondTo(variable: Variable) {
        callProcessor.setTo(variable, 1)
    }

    override fun clear() {
        callProcessor.clearResults()
    }
}
