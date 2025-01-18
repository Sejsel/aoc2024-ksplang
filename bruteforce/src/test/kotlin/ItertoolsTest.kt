import cz.sejsel.combinations
import cz.sejsel.combinationsWithReplacement
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder

class CombinationTests : FunSpec({
    test("combinations(3, 2)") {
        val combinations = mutableListOf<List<Int>>()
        combinations(listOf(0, 1, 2), 2) {
            combinations.add(it.toList())
        }

        combinations shouldContainExactlyInAnyOrder listOf(
            listOf(0, 1),
            listOf(0, 2),
            listOf(1, 2),
        )
    }

    test("combinations(3, 3)") {
        val combinations = mutableListOf<List<Int>>()
        combinations(listOf(0, 1, 2), 3) {
            combinations.add(it.toList())
        }

        combinations shouldContainExactlyInAnyOrder listOf(
            listOf(0, 1, 2),
        )
    }

    test("combinationsWithReplacement") {
        val combinations = mutableListOf<List<Int>>()
        combinationsWithReplacement(listOf(0, 1, 2), 2) {
            combinations.add(it.toList())
        }
        combinations shouldContainExactlyInAnyOrder listOf(
            listOf(0, 0),
            listOf(0, 1),
            listOf(0, 2),
            listOf(1, 0),
            listOf(1, 1),
            listOf(1, 2),
            listOf(2, 0),
            listOf(2, 1),
            listOf(2, 2),
        )
    }
})
