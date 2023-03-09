package com.kcchatapp.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import model.ChatEvent
import model.MessageEvent
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class H2Db : DAOFacade {

    init {
        Database.connect(
            createHikariDataSource(
                url = "jdbc:h2:mem:test",
                driver = "org.h2.Driver",
            )
        )
        transaction {
            SchemaUtils.create(Messages)
        }
    }

    override val chatEvents: List<ChatEvent>
         get() = runBlocking {
             getChatEvents()
         }

    override suspend fun getChatEvents(): List<ChatEvent> = dbQuery {
        Messages.selectAll().map {
            MessageEvent(
                it[Messages.username],
                it[Messages.text],
                it[Messages.timestamp].toKotlinInstant(),
            )
        }
    }

    override suspend fun saveChatEvent(chatEvent: ChatEvent) {
        dbQuery {
            if (chatEvent is MessageEvent) {
                Messages.insert {
                    it[text] = chatEvent.messageText
                    it[username] = chatEvent.username
                    it[timestamp] = chatEvent.timestamp.toJavaInstant()
                }
            }
        }
    }
}

private fun createHikariDataSource(
    url: String,
    driver: String,
) = HikariDataSource(HikariConfig().apply {
    driverClassName = driver
    jdbcUrl = url
    maximumPoolSize = 3
    isAutoCommit = false
    transactionIsolation = "TRANSACTION_REPEATABLE_READ"
    validate()
})

object Messages : Table() {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 1024)
    val text = varchar("text", 1024)
    val timestamp = timestamp("timestamp")

    override val primaryKey = PrimaryKey(id)
}

private suspend fun <T> dbQuery(block: () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }
