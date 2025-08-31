package cz.sejsel.ksplang.debugger

import cz.sejsel.ksplang.builder.AnnotatedKsplangTree
import cz.sejsel.ksplang.builder.Ksplang
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.builder.toRunnableProgram
import cz.sejsel.ksplang.dsl.core.call
import cz.sejsel.ksplang.dsl.core.program
import cz.sejsel.ksplang.interpreter.PiDigits
import cz.sejsel.ksplang.interpreter.State
import cz.sejsel.ksplang.interpreter.parseProgram
import cz.sejsel.ksplang.std.add
import cz.sejsel.ksplang.std.mul
import cz.sejsel.ksplang.std.push
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

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080, host = "localhost") {
        install(WebSockets)
        routing {
            get("/") {
                call.respondText("CS CS lensum CS funkcia ++ praise")
            }
            webSocket("/ws") {
                var stack = listOf(1L, 2L, 3L)
                var program = testingProgram().toAnnotatedTree()
                var step = 0L

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
                            ip = state.getCurrentIp(),
                            stack = state.getStack(),
                            reversed = state.isReversed(),
                        )
                    )
                }

                fun runToStep() {
                    // Big beware: operationsRun may increase multiple times in one step (during deez).
                    state = initializeState()
                    while (state.operationsRun() < step) {
                        state.runNextOp()
                    }
                }

                runToStep()
                sendState()

                while (true) {
                    when (val request = receiveDeserialized<FrontendRequest>()) {
                        is FrontendRequest.SetProgram -> {
                            program = request.program
                            state = initializeState()
                            runToStep()
                            sendState()
                        }

                        is FrontendRequest.SetStack -> {
                            stack = request.stack
                            state = initializeState()
                            runToStep()
                            sendState()
                        }

                        is FrontendRequest.StepTo -> {
                            step = request.executedInstructions
                            sendState()
                            runToStep()
                        }
                    }
                }
            }
        }
    }
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
}

@Serializable
sealed interface StateMessage {
    @Serializable
    @SerialName("state")
    data class NewState(
        val ip: Int,
        val stack: List<Long>,
        val reversed: Boolean,
    ) : StateMessage
}