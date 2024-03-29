package com.kcchatapp

import com.kcchatapp.chat.Chat
import io.ktor.server.application.*
import com.kcchatapp.plugins.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 9010, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

@Suppress("unused")
fun Application.module() {
    val chat = Chat(replay = 100)
    configureSockets(chat)
    configureRouting()
}
