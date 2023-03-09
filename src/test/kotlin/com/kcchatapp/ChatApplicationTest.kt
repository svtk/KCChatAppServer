package com.kcchatapp

import com.kcchatapp.db.DAOFacade
import com.kcchatapp.plugins.configureSockets
import com.kcchatapp.plugins.db
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import model.MessageEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlinx.datetime.*
import model.ChatEvent
import org.awaitility.Awaitility
import org.junit.jupiter.api.fail
import java.util.concurrent.TimeUnit


class ChatApplicationTest {

    /**
     * This is an integration test that verifies the behaviour of a simple conversation with an empty server.
     */
    @Test
    fun testSimpleConversation() {
        testApplication {
            lateinit var dao: DAOFacade
            application {
                dao = db()
                configureSockets()
                assertThat(runBlocking { dao.getChatEvents() }).isEmpty()
            }

            val client = createClient {
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                    })
                }
                install(WebSockets) {
                    contentConverter = KotlinxWebsocketSerializationConverter(Json)
                }
            }

            client.ws("/chat") {
                sendSerialized(MessageEvent("Anton", "Hello!", Clock.System.now()) as ChatEvent)
            }

            //region await
            Awaitility.await("all messages were saved to the database")
                .atMost(2, TimeUnit.SECONDS)
                .until {
                    runBlocking {
                        val messages = dao.getChatEvents()
                        messages.size == 1
                    }
                }
            //endregion

            client.ws("/chat") {
                val event = receiveDeserialized<ChatEvent>()
                if (event is MessageEvent) {
                    assertThat(event.username).isEqualTo("Anton")
                    assertThat(event.messageText).isEqualTo("Hello!")
                } else {
                    fail(Exception("Illegal type of event: ${event::class}"))
                }
            }

        }
    }
}
