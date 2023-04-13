package com.kcchatapp.plugins

import com.kcchatapp.chat.Chat
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import model.ChatEvent
import java.time.Duration

fun Application.configureSockets(chat: Chat) {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }

    routing {
        webSocket("/chat") {
            handleSocket(this, chat)
        }
    }
}

suspend fun handleSocket(socket: WebSocketServerSession, chat: Chat) {
    coroutineScope {
        launch {
            chat.subscribeToEvents { event ->
                socket.sendSerialized(event)
            }
        }
        launch {
            while (true) {
                val event = socket.receiveDeserialized<ChatEvent>()
                chat.broadcastToOtherClients(event)
            }
        }
    }
}