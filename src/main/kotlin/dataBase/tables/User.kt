package dataBase.tables

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.DateTime

object UserTable: Table("User"){
    val id = integer("id").autoIncrement()
    val userID = varchar("userID",32)
    val description = varchar("description", 255)
    val userCreated = datetime("userCreated")
    override val primaryKey = PrimaryKey(id)
}

class User(id: Int, userID: String, description: String, userCreated: DateTime){
    constructor(it: ResultRow): this(it[UserTable.id], it[UserTable.userID], it[UserTable.description], it[UserTable.userCreated])
}