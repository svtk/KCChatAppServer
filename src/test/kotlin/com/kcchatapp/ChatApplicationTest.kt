package com.kcchatapp

import com.kcchatapp.chat.Chat
import com.kcchatapp.plugins.configureSockets
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import model.ChatEvent
import model.MessageEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail


class ChatApplicationTest {

    /**
     * This is an integration test that verifies the behaviour of a simple conversation with an empty server.
     */
    @Test
    fun testSimpleConversation() {
        testApplication {
            application {
                configureSockets(Chat())
//                assertThat(runBlocking { dao.subscribeToChatEvents() }).isEmpty()
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
            /*Awaitility.await("all messages were saved to the database")
                .atMost(2, TimeUnit.SECONDS)
                .until {
                    runBlocking {
                        val messages = dao.getChatEvents()
                        messages.size == 1
                    }
                }*/
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
