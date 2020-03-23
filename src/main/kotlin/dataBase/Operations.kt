package dataBase

import ApplicationRegex
import dataBase.tables.*
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

object Operations {
    private val logger = KotlinLogging.logger {}
    suspend fun parseAndInsertTweet(tweet: String, userID: Int) {
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
                        ApplicationRegex.isSubtweetRegex.findAll(currHashTag).map { it.groupValues[1] }
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

    //this should only be called within a transaction, allowing a transaction to be composed of functions :)
    private fun insertTweet(transaction: Transaction, tweet: String, userID: Int): Tweet {

        // note this will never be returned either the function will return a row, or there will be an exception
        // it is required that this have a value though by the compiler
        var ret: Tweet = Tweet(1, 2, "", DateTime.now())
        with(transaction) {
            try {
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
    Operations.parseAndInsertTweet("TEST #notASubtweet #sub##tweet @jocksjig ", 3)
}