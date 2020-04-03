package api

import dataBase.DatabaseFactory
import dataBase.tables.*
import kotlinx.coroutines.selects.select
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import javax.xml.crypto.Data

// a Full tweet consists of the regular tweet metadata
// as well as if it is a retweet the original poster
// and finally the favorites and retweets
data class FullTweet(
    val tweetID: Int,
    val text: String,
    val userID: Int,
    val timestamp: DateTime,

    val user_name: String,

    val isRetweet: Boolean,
    val originalPoster: String,
    val originalPosterID: Int,

    val isReply: Boolean,
    val repliedTo: String,
    val repliedToID: Int,

    val favorites: Int,
    val retweets: Int,

    val isFavorited: Boolean,
    val isRetweeted: Boolean
) {
    companion object {
        suspend fun tweetToFullTweet(tweet: Tweet, userID: Int): FullTweet {
            var retweetData: Triple<Boolean, String, Int> = Triple(false, "", 0)
            var replyData: Triple<Boolean, String, Int> = Triple(false, "", 0)
            var favorites = 0
            var retweets = 0
            var userName: String = ""
            var isRetweeted: Boolean = false
            var isFavorited: Boolean = false
            DatabaseFactory.dbQuery {

                transaction {
                    // I dont think these can be combined into the larger query
                    favorites = FavoritesTable.select { FavoritesTable.tweetID eq tweet.id }.count().toInt()
                    retweets = RetweetsTable.select { RetweetsTable.tweetID eq tweet.id }.count().toInt()
                    // this is a replacement for a few shorter calls that were in this
                    exec ("""
                        Select T.userID as userID, T.id as tweetID, T.timestamp as timestamp, T.text as 'text',
                               U.userID as 'userName',
                               RetweetedUser.userID as 'originalPoster', RetweetedUser.id as 'originalPosterID',
                               RepliedUser.userID as 'repliedTo', RepliedUser.id as 'repliedToID',
                               IF(R.originalID is null, false, true) as 'isReply',
                               IF(R3.userID is null, false, true) as 'isRetweet',
                               IF(F.userID is null, false, true) as 'isFavorited',
                               IF(R2.userID = $userID, true, false) as 'isRetweeted'
                        from Tweet T
                                 INNER JOIN User U on ${tweet.userID} = U.id
                                 LEFT JOIN Reply R on T.id = R.replyID
                                 LEFT JOIN Tweet T2 on T2.id = R.originalID
                                 LEFT JOIN User RepliedUser on T2.userID = RepliedUser.id
                            -- basically we want to get the original tweeter as this may just be a
                            -- retweet if this is null then we should assume it is not a retweet
                                 LEFT JOIN Retweet R3 on R3.userID = ${tweet.userID} and R3.tweetID = T.id
                                 LEFT JOIN User RetweetedUser on T.userID = RetweetedUser.id
                                 LEFT JOIN Retweet R2 on R2.userID = $userID and R2.tweetID = T.id
                                 LEFT JOIN Favorite F on F.userID = $userID and F.tweetID = T.id
                        where T.id = ${tweet.id}
                    """.trimIndent()) {
                        while(it.next()) {
                            retweetData = Triple(it.getBoolean("isRetweet"), it.getString("originalPoster") ?: "",
                                it.getInt("originalPosterID")
                            )
                            replyData = Triple(it.getBoolean("isReply"), it.getString("repliedTo") ?: "",
                                it.getInt("repliedToID")
                            )
                            // you cannot favorite/retweet a retweeted tweet in my social media
                            isRetweeted = it.getBoolean("isRetweeted")
                            isFavorited = it.getBoolean("isFavorited")
                            userName = it.getString("userName")
                        }
                    }
                }
            }
            return FullTweet(
                tweet.id,
                tweet.text,
                tweet.userID,
                tweet.timestamp,
                userName,
                retweetData.first,
                retweetData.second,
                retweetData.third,
                replyData.first,
                replyData.second,
                replyData.third,
                favorites,
                retweets,
                isFavorited,
                isRetweeted
            )
        }
    }


}

suspend fun main() {
    DatabaseFactory.init()
    var tweet: Tweet = Tweet(119131, 1, "", DateTime.now())
//    DatabaseFactory.dbQuery { tweet = Tweet(TweetTable.select { TweetTable.id eq 1 }.first()) }
    println(FullTweet.tweetToFullTweet(tweet, 1))
}