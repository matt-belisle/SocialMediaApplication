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
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.JacksonConverter
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.joda.time.DateTime
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

data class ReceiveTweet(val tweet: String)
data class ReceiveDescription(val description: String)

fun Application.module() {
    install(CORS) {
        method(HttpMethod.Delete)
        header(HttpHeaders.ContentType)
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
        route("tweets/replyChain/{tweetID}/{userID}") {
            get {
                val tweetID = call.parameters["tweetID"]!!.toInt()
                val userID = call.parameters["userID"] !!.toInt()
                call.respond(Operations.getReplyChain(tweetID,userID))
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
        route("Followers/{UserID}"){
            get {
                val userID = call.parameters["userID"] !!.toInt()
                call.respond(Operations.followers(userID))
            }
        }
        route("Following/{UserID}"){
            get{
                val userID = call.parameters["userID"] !!.toInt()
                call.respond(Operations.followedUsers(userID))
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
        route("user/description/{userID}") {
            post {
                val userID = call.parameters["userID"]!!.toInt()
                val description = call.receive<ReceiveDescription>()
                Operations.updateDescription(userID,description.description)
                call.respond(200)
            }
        }
        route("follow/{follower}/{followed}") {
            get {
                val follower = call.parameters["follower"]!!.toInt()
                val followed = call.parameters["followed"]!!.toInt()
                call.respond(Operations.doesFollow(follower, followed))
            }
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
        route("favorite/tweet/{tweetID}") {
            get {
                val tweetID = call.parameters["tweetID"]!!.toInt()
                call.respond(Operations.getFavoritedUsers(tweetID))
            }
            route("{userID}") {
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
        }
        route("retweet/tweet/{tweetID}") {
            get {
                val tweetID = call.parameters["tweetID"]!!.toInt()
                call.respond(Operations.getRetweetedUsers(tweetID))
            }
            route("{userID}") {
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
        }
        route("retweet/count/{tweetID}") {
            get {
                val tweetID = call.parameters["tweetID"]!!.toInt()
                call.respond(Operations.countRetweets(tweetID))
            }
        }
        route("tweet/{userID}/{replyTo?}") {
            post {
                val userID = call.parameters["userID"]!!.toInt()
                val replyTo = call.parameters["replyTo"]
                val post = call.receive<ReceiveTweet>()
                // where -1 is not replied to
                Operations.parseAndInsertTweet(post.tweet, userID, replyTo?.toInt() ?: -1)
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
        route("hashtag/{userID}/{hashtag}/{subHashtags}") {
            get {
                //get the #s back
                // may be inappropriate blocking call but its needed to get the hashtags back (among other entities)...
                val hashTag = URLDecoder.decode(call.parameters["hashtag"]!!, StandardCharsets.UTF_8.toString())
                val userID = call.parameters["userID"]!!.toInt()
                val subHashtags = call.parameters["subHashtags"]!!.toBoolean()
                call.respond(Operations.getTweetsForHashTag(hashTag,userID,subHashtags))
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