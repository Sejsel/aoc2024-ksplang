package cz.sejsel.ksplang

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.engine.concurrency.SpecExecutionMode

object TestProjectConfig : AbstractProjectConfig() {
    override val specExecutionMode = SpecExecutionMode.Concurrent
}