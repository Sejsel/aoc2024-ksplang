package cz.sejsel.ksplang.wasm

import com.dylibso.chicory.runtime.Memory
import cz.sejsel.MemoryChunk
import cz.sejsel.MemoryChunker
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class MemoryChunkerTests : FunSpec({
    test("all zeroes") {
        val memory = mockk<Memory>()
        every { memory.pages() } returns 2
        every { memory.read(any()) } returns 0.toByte()
        val result = MemoryChunker.chunkMemory(memory, 100)
        result shouldHaveSize 1
        result.first() shouldBe MemoryChunk.Zeroes(65536 * 2)
    }

    test("one non-zero") {
        val memory = mockk<Memory>()
        every { memory.pages() } returns 2
        every { memory.read(any()) } returns 0.toByte()
        every { memory.read(8) } returns 42.toByte()
        val result = MemoryChunker.chunkMemory(memory, 100)
        result shouldHaveSize 10
        (0..7).forEach {
            result[it] shouldBe MemoryChunk.Element(0)
        }
        result[8] shouldBe MemoryChunk.Element(42)
        result[9] shouldBe MemoryChunk.Zeroes(65536 * 2 - 9)
    }

    test("zero run exactly at threshold and smaller run below threshold") {
        val memory = mockk<Memory>()
        every { memory.pages() } returns 1
        every { memory.read(any()) } returns 0.toByte()
        every { memory.read(5) } returns 11.toByte()
        every { memory.read(10) } returns 22.toByte()

        val result = MemoryChunker.chunkMemory(memory, 5)
        // Expected: Zeroes(5), Element(11), 4x Element(0), Element(22), Zeroes(65525)
        result shouldHaveSize 8
        (result[0] as MemoryChunk.Zeroes).count shouldBe 5
        (result[1] as MemoryChunk.Element).long shouldBe 11
        (2..5).forEach { idx -> (result[idx] as MemoryChunk.Element).long shouldBe 0 }
        (result[6] as MemoryChunk.Element).long shouldBe 22
        (result[7] as MemoryChunk.Zeroes).count shouldBe (65536 - 11)
    }

    test("consecutive non-zeros at start then large zero run") {
        val memory = mockk<Memory>()
        every { memory.pages() } returns 1
        every { memory.read(any()) } returns 0.toByte()
        (0..4).forEach { i -> every { memory.read(i) } returns (i + 1).toByte() }

        val result = MemoryChunker.chunkMemory(memory, 5)
        // Expected: Elements 1..5, Zeroes(65531)
        result shouldHaveSize 6
        (0..4).forEach { i -> (result[i] as MemoryChunk.Element).long shouldBe (i + 1).toByte() }
        (result[5] as MemoryChunk.Zeroes).count shouldBe (65536 - 5)
    }

    test("multiple large zero runs separated by single non-zero bytes") {
        val memory = mockk<Memory>()
        every { memory.pages() } returns 1
        every { memory.read(any()) } returns 0.toByte()
        every { memory.read(10000) } returns 7.toByte()
        every { memory.read(20000) } returns 8.toByte()

        val result = MemoryChunker.chunkMemory(memory, 1000)
        // Expected: Zeroes(10000), Element(7), Zeroes(9999), Element(8), Zeroes(45535)
        result shouldHaveSize 5
        (result[0] as MemoryChunk.Zeroes).count shouldBe 10000
        (result[1] as MemoryChunk.Element).long shouldBe 7
        (result[2] as MemoryChunk.Zeroes).count shouldBe 9999
        (result[3] as MemoryChunk.Element).long shouldBe 8
        (result[4] as MemoryChunk.Zeroes).count shouldBe (65536 - 20001)
        // Sum of counts + element bytes should equal memory size
        val total = result.sumOf { when (it) { is MemoryChunk.Element -> 1; is MemoryChunk.Zeroes -> it.count } }
        total shouldBe 65536
    }

    test("all zeroes with threshold 1 (entire memory chunks)") {
        val memory = mockk<Memory>()
        every { memory.pages() } returns 1
        every { memory.read(any()) } returns 0.toByte()
        val result = MemoryChunker.chunkMemory(memory, 1)
        result shouldHaveSize 1
        (result.first() as MemoryChunk.Zeroes).count shouldBe 65536
    }

    test("leading small zero run below threshold then non-zero then large trailing run") {
        val memory = mockk<Memory>()
        every { memory.pages() } returns 1
        every { memory.read(any()) } returns 0.toByte()
        every { memory.read(10) } returns 5.toByte()
        val result = MemoryChunker.chunkMemory(memory, 20)
        // Expect 10 Element(0), Element(5), Zeroes(65536-11)
        result shouldHaveSize 12
        (0 until 10).forEach { (result[it] as MemoryChunk.Element).long shouldBe 0 }
        (result[10] as MemoryChunk.Element).long shouldBe 5
        (result[11] as MemoryChunk.Zeroes).count shouldBe (65536 - 11)
    }

    test("tail zero run exactly at threshold size") {
        val memory = mockk<Memory>()
        every { memory.pages() } returns 1
        every { memory.read(any()) } returns 0.toByte()
        val tailThreshold = 10
        val pos = 65536 - tailThreshold - 1 // single non-zero so that remaining zeros == threshold
        every { memory.read(pos) } returns 9.toByte()
        val result = MemoryChunker.chunkMemory(memory, tailThreshold)
        // Expect Zeroes(pos), Element(9), Zeroes(tailThreshold)
        result shouldHaveSize 3
        (result[0] as MemoryChunk.Zeroes).count shouldBe pos
        (result[1] as MemoryChunk.Element).long shouldBe 9
        (result[2] as MemoryChunk.Zeroes).count shouldBe tailThreshold
    }

    test("tiny zero runs between single non-zeros stay uncompressed while large tail compresses") {
        val memory = mockk<Memory>()
        every { memory.pages() } returns 1
        every { memory.read(any()) } returns 0.toByte()
        every { memory.read(0) } returns 1.toByte()
        every { memory.read(2) } returns 2.toByte()
        val result = MemoryChunker.chunkMemory(memory, 3)
        // Pattern: 1,0,2 then big zero run
        result shouldHaveSize 4
        (result[0] as MemoryChunk.Element).long shouldBe 1
        (result[1] as MemoryChunk.Element).long shouldBe 0
        (result[2] as MemoryChunk.Element).long shouldBe 2
        (result[3] as MemoryChunk.Zeroes).count shouldBe (65536 - 3)
    }
})