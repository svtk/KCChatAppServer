package com.kcchatapp.db

import model.ChatEvent
import model.TypingEvent
import java.util.*

class InMemoryDb: DAOFacade {
    private val _messageEvents =
        Collections.synchronizedList(mutableListOf<ChatEvent>())

    override suspend fun saveChatEvent(chatEvent: ChatEvent) {
        if (chatEvent is TypingEvent) return
        _messageEvents += chatEvent
    }

    override suspend fun getChatEvents(): List<ChatEvent> = chatEvents

    override val chatEvents: List<ChatEvent>
        get() = _messageEvents
}