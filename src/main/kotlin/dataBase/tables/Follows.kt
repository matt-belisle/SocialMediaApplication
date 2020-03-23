package dataBase.tables

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object FollowsTable: Table("Follows"){
    val userIDFollower = integer("userIDFollower") references UserTable.id
    val userIDFollowed = integer("userIDFollowed") references UserTable.id
    override val primaryKey = PrimaryKey(userIDFollower, userIDFollowed)
}

data class Follows(val follower: Int, val followed: Int){
    constructor(it: ResultRow): this(it[FollowsTable.userIDFollower], it[FollowsTable.userIDFollowed])
}