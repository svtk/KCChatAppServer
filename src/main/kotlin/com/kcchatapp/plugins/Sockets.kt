package com.kcchatapp.plugins

import com.kcchatapp.Connection
import com.kcchatapp.db.DAOInMemoryImpl
import model.ChatEvent
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import java.time.Duration
import java.util.*

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }
    val dao = DAOInMemoryImpl()
    routing {
        val connections = Collections.synchronizedSet<Connection>(LinkedHashSet())
        webSocket("/chat") {
            val thisConnection = Connection(this)
            connections += thisConnection
            try {
                dao.chatEvents
                    .forEach { event -> sendSerialized(event) }
                for (content in incoming) {
                    converter?.deserialize<ChatEvent>(content)?.let { event ->
                        println("Received event: $event")
                        dao.saveChatEvent(event)
                        connections.forEach {
                            it.session.sendSerialized(event)
                        }
                    }
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
            } finally {
                connections -= thisConnection
            }
        }
    }
}
