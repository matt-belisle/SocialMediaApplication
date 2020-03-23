package dataBase.tables

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object SubHashtagTable : Table("SubHashtag"){
    val parent = varchar("parent", 100) references HashTagsTable.hashTag
    val subHashtag = varchar("subHashtag", 100) references HashTagsTable.hashTag
    override val primaryKey = PrimaryKey(parent, subHashtag)
}

data class SubHashtag(val parent: String, val subHashtag: String){
    constructor(it: ResultRow): this(it[SubHashtagTable.parent], it[SubHashtagTable.subHashtag])
}