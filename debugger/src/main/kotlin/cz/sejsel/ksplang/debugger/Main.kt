package cz.sejsel.ksplang.debugger

import cz.sejsel.ksplang.builder.AnnotatedKsplangTree
import cz.sejsel.ksplang.builder.Ksplang
import cz.sejsel.ksplang.builder.KsplangBuilder
import cz.sejsel.ksplang.dsl.core.call
import cz.sejsel.ksplang.dsl.core.program
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
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.send
import kotlinx.serialization.Serializable

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080, host = "localhost") {
        install(WebSockets)
        routing {
            get("/") {
                call.respondText("CS CS lensum CS funkcia ++ praise")
            }
            webSocket("/ws") {
                val program = testingProgram()
                val currentIp =
                send("Hi")
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
    data class SetProgram(val program: AnnotatedKsplangTree)
    data class SetStack(val stack: List<Long>)
}

sealed interface StateUpdate {
    data class NewStack(val stack: List<Long>) : StateUpdate
}