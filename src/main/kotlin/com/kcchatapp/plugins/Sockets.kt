package com.kcchatapp.plugins

import com.kcchatapp.db.DAOFacade
import com.kcchatapp.db.H2Db
import com.kcchatapp.db.InMemoryDb
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
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

    val dao = db()
    val eventsFlow = MutableSharedFlow<ChatEvent>()

    routing {
        webSocket("/chat") {
            try {
                // previous events
                dao.getChatEvents().forEach { event ->
                    sendSerialized(event)
                }
                launch {
                    // subscribing to new events
                    eventsFlow.collect {
                        sendSerialized(it)
                    }
                }
                while (true) {
                    val event = receiveDeserialized<ChatEvent>()
                    println("Received event: $event")
                    // saving incoming event
                    dao.saveChatEvent(event)
                    // emitting this event to all other websockets
                    eventsFlow.emit(event)
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
            }
        }
    }
}

fun Application.db(): DAOFacade =
    when (System.getenv("CHATDB") ?: "H2") {
        "MEM" -> InMemoryDb()
        "H2" -> H2Db()
        else -> InMemoryDb()
    }.also {
        log.info("Chat database: ${it::class.qualifiedName}")
    }
