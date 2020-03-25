package api

import dataBase.DatabaseFactory
import dataBase.Operations
import dataBase.tables.Tweet
import dataBase.tables.TweetTable
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
import io.ktor.routing.param
import io.ktor.routing.route
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.selectAll
import javax.xml.crypto.Data

fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging)
    install(Routing) {
        //TODO add pagination, just add a page number to the route and just make each page 20
        //to get tweets by either a person or just the newest 10 tweets
        route("tweets/{userID?}") {
            get {
                val tweets = mutableListOf<FullTweet>()
                val userID: String? = call.parameters["userID"]
                if(userID == null){
                    //get all tweets
                    runBlocking {
                        DatabaseFactory.dbQuery {
                            TweetTable.selectAll().limit(10).orderBy(TweetTable.timestamp).forEach { runBlocking {tweets.add(FullTweet.tweetToFullTweet(Tweet(it)))} }
                        }
                    }
                } else {
                    //TODO defensive here not assuming this will parse out correctly
                    runBlocking {
                        tweets.addAll(Operations.getTweetsByUser(userID.toInt()))
                    }
                }
                call.respond(tweets)
            }
        }
    }
    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(JacksonObjectMapper.defaultMapper))
    }
}
fun main(args: Array<String>) {
    DatabaseFactory.init()
    embeddedServer(Netty, 8080, watchPaths = listOf("BlogAppKt"), module = Application::module).start()
}