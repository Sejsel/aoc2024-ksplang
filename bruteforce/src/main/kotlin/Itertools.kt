package cz.sejsel

// Because there just isn't enough itertools in the world.

/**
 * Generates all possible combinations of the given options of the given size.
 *
 * Example: `combinations(listOf("a", "b", "c"), 2)` will generate the following.
 * Notice that the first element is the one that changes the fastest.
 * ```
 * ["a", "a"]
 * ["b", "a"]
 * ["c", "a"]
 * ["a", "b"]
 * ["b", "b"]
 * ["c", "b"]
 * ["a", "c"]
 * ["b", "c"]
 * ["c", "c"]
 * ```
 *
 * The block is called for each combination, do not store the list
 * outside of the block, it will be reused for performance.
 *
 * @throws ArithmeticException if the number of combinations is too large to fit in a Long.
 */
fun<T> combinationsWithReplacement(options: List<T>, size: Int, block: (combination: List<T>) -> Unit) {
    val combinations = powExact(options.size.toLong(), size)

    val list = mutableListOf<T>()

    (0..<combinations).forEach { i ->
        var k = i
        (0 until size).forEach { j ->
            val opIndex = k % options.size
            k /= options.size
            list.add(options[opIndex.toInt()])
        }
        block(list)
        list.clear()
    }
}

fun <T> combinations(elements: List<T>, combinationSize: Int, block: (List<T>) -> Unit) {
    val n = elements.size
    if (combinationSize > n || combinationSize < 0) return
    
    val indices = IntArray(combinationSize) { it } // Initial indices: [0, 1, ..., combinationSize-1]
    
    while (true) {
        // Build the current combination
        block(indices.map { elements[it] })

        // Find the rightmost index that can be incremented
        var i = combinationSize - 1
        while (i >= 0 && indices[i] == i + n - combinationSize) i--
        
        // If no such index exists, we've generated all combinations
        if (i < 0) break
        
        // Increment this index and reset subsequent indices
        indices[i]++
        for (j in i + 1 until combinationSize) {
            indices[j] = indices[j - 1] + 1
        }
    }
}


// How is this not a standard library function?
private fun pow(base: Long, exponent: Int): Long {
    var result = 1L
    repeat(exponent.toInt()) {
        result *= base
    }
    return result
}

private fun powExact(base: Long, exponent: Int): Long {
    var result = 1L
    repeat(exponent.toInt()) {
        result = Math.multiplyExact(result, base)
    }
    return result
}
