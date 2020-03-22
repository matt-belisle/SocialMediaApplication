package dataBase.tables

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object HashTagsTable: Table("HashTags"){
    val hashTag = varchar("hashTag", 100)
    override val primaryKey = PrimaryKey(hashTag)
}

class HashTags(val hashTag: String){
    constructor(it: ResultRow): this(it[HashTagsTable.hashTag])
}