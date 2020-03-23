package dataBase.tables

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object ReplyTable: Table("Reply") {
    val originalID = integer("originalID") references TweetTable.id
    val replyID = integer("replyID") references TweetTable.id
    override val primaryKey = PrimaryKey(originalID, replyID)
}

data class Reply(val originalID: Int, val replyID: Int){
    constructor(it: ResultRow): this(it[ReplyTable.originalID], it[ReplyTable.replyID])
}