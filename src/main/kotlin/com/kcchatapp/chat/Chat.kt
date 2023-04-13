package com.kcchatapp.chat

import kotlinx.coroutines.flow.MutableSharedFlow
import model.ChatEvent

class Chat(replay: Int) {
    private val eventFlow: MutableSharedFlow<ChatEvent> =
        MutableSharedFlow(replay = replay)

    suspend fun broadcastToOtherClients(event: ChatEvent) {
        eventFlow.emit(event)
    }

    suspend fun subscribeToEvents(action: suspend (ChatEvent) -> Unit) {
        eventFlow.collect { action(it) }
    }
}