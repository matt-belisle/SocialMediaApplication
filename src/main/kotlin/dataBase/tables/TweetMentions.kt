package dataBase.tables

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object TweetMentionsTable: Table("TweetMentions"){
    val userID = integer("userID") references UserTable.id
    val tweetID = integer("tweetID") references TweetTable.id
    override val primaryKey = PrimaryKey(userID, tweetID)
}

data class TweetMentions(val userID: Int, val tweetID: Int){
    constructor(it: ResultRow): this(it[TweetMentionsTable.userID], it[TweetMentionsTable.tweetID])
}