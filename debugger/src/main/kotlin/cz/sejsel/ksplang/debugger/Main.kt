package cz.sejsel.ksplang.debugger

import arrow.core.Either
import cz.sejsel.ksplang.builder.AnnotatedKsplangTree
import cz.sejsel.ksplang.builder.Ksplang
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.builder.toRunnableProgram
import cz.sejsel.ksplang.dsl.core.call
import cz.sejsel.ksplang.dsl.core.doWhileZero
import cz.sejsel.ksplang.dsl.core.program
import cz.sejsel.ksplang.interpreter.PiDigits
import cz.sejsel.ksplang.interpreter.RunError
import cz.sejsel.ksplang.interpreter.State
import cz.sejsel.ksplang.interpreter.parseProgram
import cz.sejsel.ksplang.std.add
import cz.sejsel.ksplang.std.dec
import cz.sejsel.ksplang.std.dup
import cz.sejsel.ksplang.std.mul
import cz.sejsel.ksplang.std.push
import cz.sejsel.ksplang.std.stacklen
import cz.sejsel.ksplang.std.swap2
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.webSocket
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080, host = "localhost") {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
        routing {
            get("/") {
                call.respondText("CS CS lensum CS funkcia ++ praise")
            }
            webSocket("/ws") {
                var stack = listOf(1L, 2L, 3L)
                var program = testingProgram2().toAnnotatedTree()
                var step = 0L

                var lastError: RunError? = null

                fun initializeState() = State(
                    ops = parseProgram(program.toRunnableProgram()),
                    initialStack = stack,
                    maxStackSize = 10_000_000,
                    piDigits = PiDigits.digits,
                )

                var state = initializeState()

                suspend fun sendState() {
                    sendSerialized<StateMessage>(
                        StateMessage.NewState(
                            program = program,
                            ip = state.getCurrentIp(),
                            step = step,
                            stack = state.getStack(),
                            reversed = state.isReversed(),
                            error = lastError?.toString()
                        )
                    )
                }

                fun runToStep() {
                    // Big beware: operationsRun may increase multiple times in one step (during deez).
                    lastError = null
                    state = initializeState()
                    while (state.operationsRun() < step) {
                        when (val result = state.runNextOp()) {
                            is Either.Left<RunError> -> {
                                lastError = result.value
                                step = state.operationsRun()
                                return
                            }
                            is Either.Right<Boolean> -> {
                                val terminate = result.value
                                if (terminate) {
                                    step = state.operationsRun()
                                    return
                                }
                            }
                        }
                    }
                }

                runToStep()
                sendState()

                while (true) {
                    when (val request = receiveDeserialized<FrontendRequest>()) {
                        is FrontendRequest.SetProgram -> {
                            log.info("Setting program to ${request.program.toString().take(100)}...")
                            program = request.program
                            state = initializeState()
                            runToStep()
                            sendState()
                        }

                        is FrontendRequest.SetStack -> {
                            log.info("Setting stack to ${request.stack.size} elements")
                            stack = request.stack
                            state = initializeState()
                            runToStep()
                            sendState()
                        }

                        is FrontendRequest.StepTo -> {
                            log.info("Stepping to ${request.executedInstructions} (currently at $step)")
                            step = request.executedInstructions
                            runToStep()
                            sendState()
                        }

                        FrontendRequest.RunToEnd -> {
                            log.info("Running to end")
                            step = Long.MAX_VALUE
                            runToStep()
                            step = state.operationsRun()
                            sendState()
                        }

                        is FrontendRequest.RunToInstruction -> {
                            log.info("Running to instruction ${request.instructionIndex} forwards")
                            val targetIp = request.instructionIndex
                            val originalStep = step
                            state = initializeState()
                            lastError = null
                            step = 0
                            var instructionReached = false
                            while (true) {
                                if (state.getCurrentIp() == targetIp && state.operationsRun() >= request.fromStep) {
                                    instructionReached = true
                                    break
                                }
                                when (val result = state.runNextOp()) {
                                    is Either.Left<RunError> -> {
                                        lastError = result.value
                                        break
                                    }

                                    is Either.Right<Boolean> -> {
                                        val terminate = result.value
                                        if (terminate) break
                                    }
                                }
                                step = state.operationsRun()
                            }

                            if (!instructionReached) {
                                log.info("... never occurred")
                                step = originalStep
                                runToStep()
                            }

                            sendState()
                        }

                        is FrontendRequest.RunToInstructionBackwards -> {
                            // To make this reasonably efficient, what we do is:
                            // Run from start, keep track of last step when we were at the target instruction
                            // Stop at toStep
                            // Rerun until lastStep if it occurred.
                            log.info("Running to instruction ${request.instructionIndex} backwards")
                            val originalStep = step
                            val targetIp = request.instructionIndex
                            state = initializeState()
                            lastError = null
                            step = 0
                            var lastStepAtTarget: Long? = null
                            while (true) {
                                if (state.getCurrentIp() == targetIp && state.operationsRun() <= request.fromStep) {
                                    lastStepAtTarget = state.operationsRun()
                                }
                                if (state.operationsRun() >= request.fromStep) break
                                when (val result = state.runNextOp()) {
                                    is Either.Left<RunError> -> {
                                        lastError = result.value
                                        break
                                    }

                                    is Either.Right<Boolean> -> {
                                        val terminate = result.value
                                        if (terminate) break
                                    }
                                }
                            }

                            if (lastStepAtTarget != null) {
                                log.info("... last occurrence at $lastStepAtTarget, rerunning")
                                step = lastStepAtTarget
                                runToStep()
                            } else {
                                log.info("... never occurred")
                                step = originalStep
                                runToStep()
                            }
                            sendState()
                        }
                    }
                }
            }
        }
    }.start(wait = true)
}

fun testingProgram(): Ksplang {
    val builder = KsplangBuilder()
    val ksplang = program {
        val double = function2To1("double") {
            mul(2)
        }

        body {
            call(double)
            push(13)
            add()
        }
    }

    return builder.buildAnnotated(ksplang)
}

fun testingProgram2(): Ksplang {
    val builder = KsplangBuilder()
    val ksplang = program {
        val double = function1To1("double") {
            mul(2)
        }

        body {
            stacklen()
            call(double)
            call(double)
            call(double)
            call(double)
            // len*16
            doWhileZero {
                dec()
                push(13)
                swap2()
                dup()
            }
        }
    }

    return builder.buildAnnotated(ksplang)
}

@Serializable
sealed interface FrontendRequest {
    @Serializable
    @SerialName("set_program")
    data class SetProgram(val program: AnnotatedKsplangTree) : FrontendRequest

    @Serializable
    @SerialName("set_stack")
    data class SetStack(val stack: List<Long>) : FrontendRequest

    @Serializable
    @SerialName("step_to")
    data class StepTo(
        /** Target number of executed instructions (absolute). */
        val executedInstructions: Long
    ) : FrontendRequest

    @Serializable
    @SerialName("run_to_end")
    object RunToEnd : FrontendRequest

    @Serializable
    @SerialName("run_to_instruction")
    data class RunToInstruction(val fromStep: Long, val instructionIndex: Int) : FrontendRequest

    @Serializable
    @SerialName("run_to_instruction_backwards")
    data class RunToInstructionBackwards(val fromStep: Long, val instructionIndex: Int) : FrontendRequest
}

@Serializable
sealed interface StateMessage {
    @Serializable
    @SerialName("state")
    data class NewState(
        val program: AnnotatedKsplangTree,
        val ip: Int,
        val step: Long,
        val stack: List<Long>,
        val reversed: Boolean,
        val error: String?,
    ) : StateMessage
}