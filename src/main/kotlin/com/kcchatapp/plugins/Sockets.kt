package com.kcchatapp.plugins

import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import model.ChatEvent
import java.time.Duration

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }

    val eventsFlow = MutableSharedFlow<ChatEvent>(replay = 10)

    routing {
        webSocket("/chat") {
            coroutineScope {
                launch {
                    eventsFlow.collect { event ->
                        sendSerialized(event)
                    }
                }
                launch {
                    while (true) {
                        val event = receiveDeserialized<ChatEvent>()
                        eventsFlow.emit(event)
                    }
                }
            }
        }
    }
}