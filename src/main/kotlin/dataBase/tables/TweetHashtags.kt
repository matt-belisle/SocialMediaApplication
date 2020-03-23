package dataBase.tables

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object TweetHashtagsTable: Table("TweetHashtags") {
    val hashTag = varchar("hashTag", 100) references HashTagsTable.hashTag
    val tweetID = integer("tweetID") references TweetTable.id
    override val primaryKey = PrimaryKey(
        hashTag,
        tweetID
    )
}

data class TweetHashtags(val hashTag: String, val tweetID: Int){
    constructor(it: ResultRow): this(it[TweetHashtagsTable.hashTag], it[TweetHashtagsTable.tweetID])
}