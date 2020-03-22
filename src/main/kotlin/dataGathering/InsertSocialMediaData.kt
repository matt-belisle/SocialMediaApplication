package dataGathering

import dataBase.DatabaseFactory
import dataBase.tables.HashTagsTable
import dataBase.tables.TweetHashtagsTable
import dataBase.tables.TweetMentionsTable
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteAll

class InsertSocialMediaData {
    suspend fun insertSocialMediaData() {
        val allHashTagsForTweets = GetAllHashTags().getAllHashTags()
        //insert all of the hashtags
        val hashTags = mutableListOf<String>()
        allHashTagsForTweets.values.forEach { element -> hashTags.addAll(element) }

        DatabaseFactory.dbQuery { TweetHashtagsTable.deleteAll() }
        DatabaseFactory.dbQuery { HashTagsTable.deleteAll() }


        // arbitrary hashTag max length that is set in DB
        DatabaseFactory.dbQuery {
            HashTagsTable.batchInsert(hashTags.distinct().filter { it.length < 101 })
            { this[HashTagsTable.hashTag] = it }
        }

        //insert the hashTags for individual tweets -- ie a tweet had a hashtag in it
        // expand the map to a list of pairs to be batch inserted
        val tweetToHashTags = mutableListOf<Pair<Int, String>>()
        allHashTagsForTweets.forEach { (index, list) -> list.forEach { tweetToHashTags.add(Pair(index, it)) } }

        DatabaseFactory.dbQuery {
            TweetHashtagsTable.batchInsert(tweetToHashTags.filter { it.second.length < 101 }.distinct()) {
                this[TweetHashtagsTable.tweetID] = it.first
                this[TweetHashtagsTable.hashTag] = it.second
            }
        }
        //Insert mentions
        val allMentions = GetAllHashTags().getAllMentions()
        // expand the map to a list of pairs to be batch inserted
        val mentionsToUser = mutableListOf<Pair<Int, Int>>()
        allMentions.forEach { (tweetID, mentions) -> mentions.forEach { if(it != -1) mentionsToUser.add(Pair(tweetID,it)) } }
        DatabaseFactory.dbQuery { TweetMentionsTable.deleteAll() }
        DatabaseFactory.dbQuery { TweetMentionsTable.batchInsert(mentionsToUser.distinct()) {
            this[TweetMentionsTable.tweetID] = it.first
            this[TweetMentionsTable.userID] = it.second
        } }



    }
}

suspend fun main() {
    DatabaseFactory.init()
    InsertSocialMediaData().insertSocialMediaData()
}