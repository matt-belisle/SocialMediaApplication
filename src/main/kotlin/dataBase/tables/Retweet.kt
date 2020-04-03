package dataBase.tables

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.DateTime

object RetweetsTable: Table("Retweet"){
    val tweetID = integer("tweetID") references TweetTable.id
    val userID = integer("userID") references UserTable.id
    val timestamp = datetime("timestamp")
    override val primaryKey = PrimaryKey(userID, tweetID)
}

data class Retweet(val tweetID: Int, val userID: Int, val timestamp: DateTime){
    constructor(it: ResultRow): this(it[RetweetsTable.tweetID], it[RetweetsTable.userID], it[RetweetsTable.timestamp])
}