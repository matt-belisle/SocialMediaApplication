package dataBase.tables

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table


/*
CREATE TABLE `auspol2019Data` (
  `created_at` text,
  `id` double DEFAULT NULL,
  `full_text` text,
  `retweet_count` DECIMAL DEFAULT NULL,
  `favorite_count` DECIMAL DEFAULT NULL,
  `user_id` double DEFAULT NULL,
  `user_name` text,
  `user_screen_name` text,
  `user_description` text,
  `user_location` text,
  `user_created_at` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

 */

object AusPol2019DataTable : Table("auspol2019Data") {
    val created_at = text("created_at")
    val id = integer("id")
    val full_text = (text("full_text").nullable())
    val retweet_count = (integer("retweet_count")).nullable()
    val favorite_count = (integer("favorite_count")).nullable()
    val user_id = (integer("user_id")).nullable()
    val user_name = text("user_name").nullable()
    val user_screen_name = text("user_screen_name")
    val user_description = text("user_description").nullable()
    val user_location = text("user_location").nullable()
    val user_created_at = text("user_created_at")
    val autoID = integer("autoID")
}

class AusPol2019Data(
    val created_at: String,
    val id: Int,
    val full_text: String,
    val retweet_count: Int,
    val favorite_count: Int,
    val user_id: Int,
    val user_name: String,
    val user_screen_name: String,
    val user_description: String,
    val user_location: String,
    val user_created_at: String,
    val autoID: Int
) {
    constructor(it: ResultRow) : this(
        created_at = it[AusPol2019DataTable.created_at],
        id = it[AusPol2019DataTable.id],
        full_text = it[AusPol2019DataTable.full_text] ?: "",
        retweet_count = it[AusPol2019DataTable.retweet_count] ?: 0,
        favorite_count = it[AusPol2019DataTable.favorite_count] ?: 0,
        user_id = it[AusPol2019DataTable.user_id] ?: 0,
        user_name = it[AusPol2019DataTable.user_name] ?: "",
        user_screen_name = it[AusPol2019DataTable.user_screen_name],
        user_description = it[AusPol2019DataTable.user_description]?: "",
        user_location = it[AusPol2019DataTable.user_location]?: "",
        user_created_at = it[AusPol2019DataTable.user_created_at],
        autoID = it[AusPol2019DataTable.autoID]
    )

    override fun toString(): String {
        return "id = $id, full_text = $full_text"
    }
}