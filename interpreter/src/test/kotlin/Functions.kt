import cz.sejsel.funkcia
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.equals.shouldBeEqual

class FunkciaTests : FunSpec({
    val mod = 1_000_000_007L

    val tests = listOf<Triple<Long, Long, Long>>(
        Triple(100, 54, 675),
        Triple(54, 100, 675),
        Triple(-1, -1, 0),
        Triple(1, 0, 0),
        Triple(1, 1, 0),
        Triple(1, 2, 2),
        Triple(2, 2, 0),
        Triple(Long.MAX_VALUE, 0, Long.MAX_VALUE % mod),
        Triple(0, Long.MAX_VALUE, Long.MAX_VALUE % mod),
        Triple(0, mod, 0),
        Triple(0, mod - 1, mod - 1),
    )

    context("is in range") {
        withData(nameFn = { it.toString() }, tests) { (a, b, result) ->
            funkcia(a, b) shouldBeEqual result
        }
    }
})
