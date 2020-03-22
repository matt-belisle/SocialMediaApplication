package dataGathering

import dataBase.DatabaseFactory
import dataBase.tables.AusPol2019DataTable
import dataBase.tables.Tweet
import dataBase.tables.TweetTable
import dataBase.tables.UserTable
import org.jetbrains.exposed.sql.selectAll

val hasTagRegex = """\s#([\w_-]+)""".toRegex()
val hasMentionRegex = """\s@([\w_-]+)""".toRegex()

class GetAllHashTags {

    suspend fun getAllHashTags(): Map<Int,List<String>> {
        return getByRegex(hasTagRegex)
    }
    suspend fun getAllMentions(): Map<Int,List<Int>> {
        val users: MutableMap<String,Int> = mutableMapOf()
        // get users again should probably be cached if this werent just for setup
        DatabaseFactory.dbQuery { UserTable.slice(UserTable.userID, UserTable.id).selectAll().forEach{users[it[UserTable.userID]] = it[UserTable.id]} }
        val tweets = getByRegex(hasMentionRegex)
        //filter out the -1 later this just means the user wasnt in the data provided. Oh well :/
        return tweets.mapValues { it.value.map { userId -> users[userId] ?: -1}}

    }

    // I recognize this should be cached, but this is only done when completely wiping the DB
    private suspend fun getAllTweets(): MutableMap<Int, String> {
        val data = mutableMapOf<Int, String>()
        DatabaseFactory.dbQuery {
            (TweetTable.slice(TweetTable.id, TweetTable.text).selectAll()
                .forEach { data[it[TweetTable.id]] = it[TweetTable.text] ?: "" })
        }
        return data
    }

    private suspend fun getByRegex(regex: Regex): MutableMap<Int, List<String>> {
        val data = getAllTweets()

        val ret = mutableMapOf<Int, List<String>>()
        data.forEach {
            ret[it.key] =
                (regex.findAll(it.value).map { tag -> tag.groupValues[1].toLowerCase() }.toList())
        }
        return ret.filter { it.value.isNotEmpty() }.toMutableMap()
    }
}


suspend fun main() {
    DatabaseFactory.init()
    val logger = mu.KotlinLogging.logger {}
    val hashTags = GetAllHashTags().getAllMentions()
    logger.info(hashTags.size.toString())
//    hashTags.forEach { logger.info(it) }
}