package api

import dataBase.DatabaseFactory
import dataBase.Operations
import dataBase.tables.Tweet
import dataBase.tables.TweetTable
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.JacksonConverter
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.joda.time.DateTime
import javax.xml.crypto.Data

fun Application.module() {
    install(CORS) {
        method(HttpMethod.Delete)
        anyHost()
    }
    install(CallLogging)
    install(Routing) {
        //TODO add pagination, just add a page number to the route and just make each page 20
        //to get tweets for a user
        route("tweets/ForUser/{searchingUser}/{userID?}") {
            get {
                val searchingUser = call.parameters["searchingUser"]!!.toInt()
                val userID: String? = call.parameters["userID"]
                if (userID == null) {
                    //get all tweets
                        val tweets = mutableListOf<FullTweet>()
                        val callRef = call
                        DatabaseFactory.dbQuery {
                            TweetTable.selectAll().limit(10).orderBy(TweetTable.timestamp)
                                .forEach { runBlocking { tweets.add(FullTweet.tweetToFullTweet(Tweet(it), searchingUser)) } }
                        }
                        call.respond(tweets)
                } else {
                    //TODO defensive here not assuming this will parse out correctly
                    call.respond(Operations.getTweetsByUser(userID.toInt(),searchingUser))

                }
            }
        }
        route("login/{userName}"){
            post {
                val userName = call.parameters["userName"]!!
                val code: HttpStatusCode = if(Operations.userExists(userName)) HttpStatusCode.OK else HttpStatusCode.NotFound
                call.respond(code)
            }
        }
        route("tweet/{searchingUser}/{tweetID}"){
            get {
                val searchingUser = call.parameters["searchingUser"]!!.toInt()
                val tweetID = call.parameters["tweetID"]!!.toInt()
                var tweet = Tweet(1,1,"", DateTime.now())
                DatabaseFactory.dbQuery {
                    tweet = Tweet(TweetTable.select { TweetTable.id eq tweetID }.first())
                }
                call.respond(FullTweet.tweetToFullTweet(tweet,searchingUser))
            }
        }
        // this includes yourself
        route("tweets/ByFollowed/{userID}") {
            get {
                // must exist this time no optional
                val userID: String = call.parameters["userID"]!!
                call.respond(Operations.getFollowedTweets(userID.toInt()))
            }
        }
        route("follow/{follower}/{followed}") {
            post {
                val follower = call.parameters["follower"]!!.toInt()
                val followed = call.parameters["followed"]!!.toInt()
                Operations.followUser(follower, followed)
                call.respond(200)
            }
            delete {
                val follower = call.parameters["follower"]!!.toInt()
                val followed = call.parameters["followed"]!!.toInt()

                Operations.unfollowUser(follower, followed)
                call.respond(200)
            }
        }
        route("favorite/count/{tweetID}") {
            get {
                val tweetID = call.parameters["tweetID"]!!.toInt()
                async {
                    call.respond(Operations.countFavorites(tweetID))
                }
            }
        }
        route("favorite/tweet/{tweetID}/{userID}") {
            post {
                val tweetID = call.parameters["tweetID"]!!.toInt()
                val userID = call.parameters["userID"]!!.toInt()

                Operations.favoriteTweet(tweetID, userID)
                call.respond(200)
            }
            delete {
                val tweetID = call.parameters["tweetID"]!!.toInt()
                val userID = call.parameters["userID"]!!.toInt()
                Operations.unfavoriteTweet(tweetID, userID)
                call.respond(200)
            }
        }
        route("retweet/tweet/{tweetID}/{userID}") {
            post {
                val tweetID = call.parameters["tweetID"]!!.toInt()
                val userID = call.parameters["userID"]!!.toInt()
                Operations.retweetTweet(tweetID, userID)
                call.respond(200)
            }
            delete {
                val tweetID = call.parameters["tweetID"]!!.toInt()
                val userID = call.parameters["userID"]!!.toInt()
                Operations.unRetweetTweet(tweetID, userID)
                call.respond(200)
            }
        }
        route("retweet/count/{tweetID}") {
            get {
                val tweetID = call.parameters["tweetID"]!!.toInt()
                call.respond(Operations.countRetweets(tweetID))
            }
        }
        route("tweet/{userID}/{tweet}/{replyTo?}") {
            post {
                val userID = call.parameters["userID"]!!.toInt()
                val tweet = call.parameters["tweet"]!!
                val replyTo = call.parameters["replyTo"]
                // where -1 is not replied to
                Operations.parseAndInsertTweet(tweet, userID, replyTo?.toInt() ?: -1)
                call.respond(200)
            }
        }
        route("deleteTweet/{tweetID}") {
            delete {
                val tweetID = call.parameters["tweetID"]!!.toInt()
                Operations.deleteTweet(tweetID)
                call.respond(200)
            }
        }
        route("count") {
            get("Followers/{userID}") {
                val userID = call.parameters["userID"]!!.toInt()
                call.respond(Operations.followerCount(userID))
            }
            get("Following/{UserID}") {
                val userID = call.parameters["userID"]!!.toInt()
                call.respond(Operations.followCount(userID))
            }
        }
        route("user/{user}") {
            post {
                //in this case user is a string
                val user = call.parameters["user"]!!
                Operations.addUser(user)
                call.respond(200)
            }
            delete {
                //in this case the id is sent back to be deleted
                val user = call.parameters["user"]!!.toInt()
                Operations.deleteUser(user)
                call.respond(200)
            }
            get {
                val user = call.parameters["user"]!!
                call.respond(Operations.getUser(user))
            }
        }
        //returns all of the usernames that the database knows
        route("users/all"){
            get{
                call.respond(Operations.getAllUsers())
            }
        }
        route("hashtag/{userID}/{hashtag}") {
            get {
                val hashTag = call.parameters["hashtag"]!!
                val userID = call.parameters["userID"]!!.toInt()
                call.respond(Operations.getTweetsForHashTag(hashTag,userID))
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