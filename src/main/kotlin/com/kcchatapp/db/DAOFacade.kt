package com.kcchatapp.db

import model.ChatEvent

interface DAOFacade {
    suspend fun saveChatEvent(chatEvent: ChatEvent)

    val chatEvents: List<ChatEvent>
}