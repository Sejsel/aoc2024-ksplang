package cz.sejsel.ksplang

import cz.sejsel.ksplang.builder.CS
import cz.sejsel.ksplang.builder.SimpleFunction
import cz.sejsel.ksplang.builder.function
import cz.sejsel.ksplang.builder.inc
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SimpleFunctionTests {
    @Test
    fun `simple function flattening`() {
        val f = SimpleFunction(
            listOf(
                SimpleFunction(
                    listOf(
                        CS, inc,
                        SimpleFunction(
                            listOf(
                                CS, CS, inc, CS
                            )
                        ),
                        CS, inc
                    )
                ),
            )
        )

        assertEquals(listOf(CS, inc, CS, CS, inc, CS, CS, inc), f.getInstructions())
    }

    @Test
    fun `simple function builder flattening`() {
        val f = function {
            function {
                CS()
                inc()
                function {
                    CS()
                    CS()
                    inc()
                    CS()
                }
                CS()
                inc()
            }
        }

        assertEquals(listOf(CS, inc, CS, CS, inc, CS, CS, inc), f.getInstructions())
    }
}