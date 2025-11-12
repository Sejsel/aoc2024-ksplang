package cz.sejsel.ksplang

import io.kotest.core.config.AbstractProjectConfig

object TestProjectConfig : AbstractProjectConfig() {
    override val parallelism = 12
}