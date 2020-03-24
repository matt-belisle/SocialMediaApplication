package dataBase

import ApplicationRegex
import api.FullTweet
import dataBase.tables.*
import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

object Operations {
    private val logger = KotlinLogging.logger {}
    
    suspend fun parseAndInsertTweet(tweet: String, userID: Int, replyTo: Int) {
        //first parse out hashtags
        val hashTags: List<String> = ApplicationRegex.hasTagRegex.findAll(tweet).map { it.groupValues[1] }.toList()
        //then mentions
        val mentions: List<String> = ApplicationRegex.hasMentionRegex.findAll(tweet).map { it.groupValues[1] }.toList()

        //start a transaction, note all of these must work for it to be a successful *tweet*
        /*
        1. Add tweet to tweet table
        2. Add mentions to mention table, iff the mentioned user exists
        3. Add hashTags to hashTag table if they dont exist
        4. Add HashTag linked to tweet
        5. If the hashTag happens to be a subHashTag map that out as well
         */
        DatabaseFactory.dbQuery {
            transaction {
                try {
                    val t = insertTweet(this, tweet, userID)
                    // each mention is wrapped in a try block, we don't want to rollback if the FK check fails
                    if (replyTo != -1) {
                        // If we are trying to reply, and we can't then the tweet should fail
                        ReplyTable.insert {
                            it[ReplyTable.originalID] = replyTo
                            it[ReplyTable.replyID] = t.id
                        }
                    }
                    mentions.forEach {
                        try {
                            insertMention(this, t.id, it)
                        } catch (e: Exception) {
                            logger.error { e.localizedMessage }
                            logger.debug { e.stackTrace }
                        }
                    }
                    // similarly for hashtags, the pk check will fail if trying to add a preexisting
                    hashTags.forEach { currHashTag ->
                        insertHashTag(this, currHashTag)
                        ApplicationRegex.isSubTagRegex.findAll(currHashTag).map { it.groupValues[1] }
                            .forEach { parent ->
                                insertHashTag(this, parent)
                                //last but not least lets make the parent to sub relationship
                                insertSubHashTagRelation(this, parent, currHashTag)
                            }
                        // finally link the hashtag to the tweet, this *should* be safe, and is critical to the tweet
                        TweetHashtagsTable.insert {
                            it[tweetID] = t.id
                            it[hashTag] = currHashTag
                        }
                        val x = 1
                    }
                    rollback()
                } catch (e: Exception) {
                    // the tweet was unsuccessful for any number of reasons
                    rollback()
                    // catch this and send to front end likely
                    throw e
                }
            }
        }
    }

    suspend fun favoriteTweet(tweetID: Int, userID: Int) {
        DatabaseFactory.dbQuery {
            FavoritesTable.insert {
                it[this.userID] = userID
                it[this.tweetID] = tweetID
            }
        }
    }

    suspend fun unfavoriteTweet(tweetID: Int, userID: Int) {
        DatabaseFactory.dbQuery {
            FavoritesTable.deleteWhere {
                (FavoritesTable.tweetID eq tweetID) and (FavoritesTable.userID eq userID)
            }
        }
    }

    suspend fun countFavorites(tweetID: Int): Int {
        var count = 0
        DatabaseFactory.dbQuery {
            count = FavoritesTable.select { FavoritesTable.tweetID eq tweetID }.count().toInt()
        }
        return count
    }

    suspend fun retweetTweet(tweetID: Int, userID: Int) {
        DatabaseFactory.dbQuery {
            RetweetsTable.insert {
                it[this.userID] = userID
                it[this.tweetID] = tweetID
            }
        }
    }

    suspend fun unRetweetTweet(tweetID: Int, userID: Int) {
        DatabaseFactory.dbQuery {
            RetweetsTable.deleteWhere {
                (RetweetsTable.tweetID eq tweetID) and (RetweetsTable.userID eq userID)
            }
        }
    }

    suspend fun countRetweets(tweetID: Int): Int {
        var count = 0
        DatabaseFactory.dbQuery {
            count = RetweetsTable.select { RetweetsTable.tweetID eq tweetID }.count().toInt()
        }
        return count
    }

    suspend fun getTweetsForHashTag(hashTag: String): List<FullTweet> {
        // this will not get retweets just all tweets for a hashtag and its subHashTags (the base tweets)
        val tweets = mutableListOf<Tweet>()
        DatabaseFactory.dbQuery {
            TweetHashtagsTable.join(TweetTable, JoinType.INNER, TweetHashtagsTable.tweetID).select {
                (TweetHashtagsTable.hashTag eq hashTag) or (TweetHashtagsTable.hashTag inSubQuery (SubHashtagTable.slice(
                    SubHashtagTable.subHashtag
                ).select { SubHashtagTable.parent eq hashTag }))
            }.forEach { tweets.add(Tweet(it)) }
        }
        return tweets.map { FullTweet.tweetToFullTweet(it) }
    }

    //returns the created user
    suspend fun addUser(userName: String): User {
        var user: User = User(1, "", "", DateTime.now())
        DatabaseFactory.dbQuery {
            transaction {
                try {
                    UserTable.insert {
                        it[UserTable.description] = "Default Description"
                        it[UserTable.userID] = userName
                        it[UserTable.userCreated] = DateTime.now()
                    }
                    user = User(UserTable.select { UserTable.userID eq userName }.first())
                } catch (e: Exception) {
                    rollback()
                    throw Exception("User: $userName Already exists", e)
                }
            }
        }
        return user
    }

    // returns the updates user
    suspend fun updateDescription(userID: Int, description: String): User {
        var user: User = User(1, "", "", DateTime.now())
        DatabaseFactory.dbQuery {
            try {
                UserTable.update({ UserTable.id eq userID }) {
                    it[UserTable.description] = description
                }
                user = User(UserTable.select { UserTable.id eq userID }.first())
            } catch (e: Exception) {
                throw Exception("user with id: $userID does not exist", e)
            }
        }
        return user
    }

    suspend fun getTweetsByUser(userID: Int): List<FullTweet>{
        //returns the tweets, and retweets by a person
        val tweets = mutableListOf<Tweet>()
        DatabaseFactory.dbQuery {
            TweetTable.select { TweetTable.userID eq userID }.forEach { tweets.add(Tweet(it)) }
            TweetTable.join(RetweetsTable,JoinType.INNER, TweetTable.id, RetweetsTable.tweetID).select {
                RetweetsTable.userID eq userID
            }.forEach{tweets.add(Tweet(it[TweetTable.id], userID, it[TweetTable.text], it[TweetTable.timestamp]))}
        }
        return tweets.map { FullTweet.tweetToFullTweet(it) }
    }

    //these should only be called within a transaction, allowing a transaction to be composed of functions :)
    private fun insertTweet(transaction: Transaction, tweet: String, userID: Int): Tweet {

        // note this will never be returned either the function will return a row, or there will be an exception
        // it is required that this have a value though by the compiler
        var ret: Tweet = Tweet(1, 2, "", DateTime.now())
        with(transaction) {
            try {
                //this just returns the created tweets id, I dont think this is possible using Kotlin Exposed as of today
                exec("CALL insert_tweet(\"${tweet}\",${userID});") { result ->
                    while (result.next()) {
                        ret = Tweet(
                            result.getInt("id"),
                            result.getInt("userID"),
                            result.getString("text"),
                            DateTime(result.getDate("timestamp"))
                        )
                    }
                }
            } catch (e: Exception) {
                throw e
            }
        }
        return ret
    }

    private fun insertMention(transaction: Transaction, tweetID: Int, userName: String) {
        with(Transaction) {
            TweetMentionsTable.insert {
                it[this.userID] = User(UserTable.select { UserTable.userID eq userName }.first()).id
                it[this.tweetID] = tweetID
            }
        }
    }

    private fun insertHashTag(transaction: Transaction, hashTag: String) {
        with(Transaction) {
            try {
                HashTagsTable.insert {
                    it[HashTagsTable.hashTag] = hashTag
                }
            } catch (e: Exception) {
                logger.error { e.localizedMessage }
                logger.debug { e.stackTrace }
            }
        }
    }

    private fun insertSubHashTagRelation(transaction: Transaction, parent: String, subHashtag: String) {
        with(Transaction) {
            try {
                SubHashtagTable.insert {
                    it[SubHashtagTable.parent] = parent
                    it[SubHashtagTable.subHashtag] = subHashtag
                }
            } catch (e: Exception) {
                logger.error { e.localizedMessage }
                logger.debug { e.stackTrace }
            }
        }
    }
}


suspend fun main() {
    DatabaseFactory.init()
//    Operations.retweetTweet(500,1)
    val tweets = Operations.getTweetsByUser(1)
    println(tweets.size)
}