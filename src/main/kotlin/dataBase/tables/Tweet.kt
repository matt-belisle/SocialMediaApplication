package dataBase.tables

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.DateTime

object TweetTable: Table("Tweet"){
    val id = integer("id").autoIncrement()
    val userID = integer("userID") references UserTable.id
    val text = text("text")
    val timestamp = datetime("timestamp")
    override val primaryKey = PrimaryKey(id)
}

data class Tweet(val id: Int, val userID: Int, val text: String, val timestamp: DateTime){
    constructor(it: ResultRow): this(
        id = it[TweetTable.id],
        userID = it[TweetTable.userID],
        text = it[TweetTable.text],
        timestamp = it[TweetTable.timestamp]
    )
}