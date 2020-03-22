package dataBase.tables

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object RetweetsTable: Table("Retweet"){
    val tweetID = integer("tweetID") references TweetTable.id
    val userID = integer("userID") references UserTable.id
    override val primaryKey = PrimaryKey(userID, tweetID)
}

class Retweet(val tweetID: Int, val userID: Int){
    constructor(it: ResultRow): this(it[RetweetsTable.tweetID], it[RetweetsTable.userID])
}