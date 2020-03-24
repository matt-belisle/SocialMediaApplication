package api

import api.JacksonObjectMapper
import dataBase.tables.Tweet
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.jackson.JacksonConverter
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.joda.time.DateTime

fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging)
    install(Routing) {
        get("i/") {
            call.respond(Tweet(1,2,"This is From Jackson", DateTime.now()))
        }
    }
    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(JacksonObjectMapper.defaultMapper))
    }
}
fun main(args: Array<String>) {
    embeddedServer(Netty, 8080, watchPaths = listOf("BlogAppKt"), module = Application::module).start()
}