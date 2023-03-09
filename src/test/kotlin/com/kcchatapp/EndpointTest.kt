package com.kcchatapp

import com.kcchatapp.plugins.configureRouting
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class EndpointTest {
    @Test
    fun testRoot() = testApplication {
        application {
            configureRouting()
        }
        client.get("/").apply {
            assertThat(status).isEqualTo(HttpStatusCode.OK)
            assertThat(bodyAsText()).isEqualTo("Hello, World!")
        }
    }
}