package com.kcchatapp.chat

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import model.ChatEvent

class Chat(replay: Int = 10) {
    private val _eventFlow: MutableSharedFlow<ChatEvent> = MutableSharedFlow(replay = replay)

    val eventFlow: SharedFlow<ChatEvent> get() = _eventFlow

    suspend fun broadcastEvent(event: ChatEvent) {
        _eventFlow.emit(event)
    }
}