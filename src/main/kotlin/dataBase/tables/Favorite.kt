package dataBase.tables

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object FavoritesTable: Table("Favorites"){
    val tweetID = integer("tweetID") references TweetTable.id
    val userID = integer("userID") references UserTable.id
    override val primaryKey = PrimaryKey(userID, tweetID)
}

class Favorite(val tweetID: Int, val userID: Int){
    constructor(it: ResultRow): this(it[FavoritesTable.tweetID], it[FavoritesTable.userID])
}