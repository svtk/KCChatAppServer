package com.kcchatapp.db

import model.ChatEvent

interface DAOFacade {
    suspend fun saveChatEvent(chatEvent: ChatEvent)

    suspend fun getChatEvents(): List<ChatEvent>

    val chatEvents: List<ChatEvent>
}