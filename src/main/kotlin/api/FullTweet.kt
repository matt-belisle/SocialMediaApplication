package api

import dataBase.DatabaseFactory
import dataBase.tables.*
import kotlinx.coroutines.selects.select
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
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
        //TODO less DB queries, theres a lot of queries going on here...
        suspend fun tweetToFullTweet(tweet: Tweet, userID: Int): FullTweet {
            var retweetData: Triple<Boolean, String, Int> = Triple(false, "", 0)
            var replyData: Triple<Boolean, String, Int> = Triple(false, "", 0)
            var favorites = 0
            var retweets = 0
            var userName: String = ""
            var isRetweeted: Boolean = false
            var isFavorited: Boolean = false

            // so a retweet is just the original tweets id + the new tweeters id, so to see if it is a retweet
            // see if it is in the table

            DatabaseFactory.dbQuery {
                //begin retweet data
                var retweetUserID: Int = 0
                var retweetUserName = ""
                val isRetweet =
                    RetweetsTable.select { (RetweetsTable.userID eq tweet.userID) and (RetweetsTable.tweetID eq tweet.id) }
                        .count() > 0
                if (isRetweet) {
                    // note only one thing will return
                    val row = (UserTable.join(TweetTable, JoinType.INNER, UserTable.id, TweetTable.userID)
                        .slice(UserTable.id, UserTable.userID).select {
                            TweetTable.id eq tweet.id
                        }).first()
                    retweetUserID = row[UserTable.id]
                    retweetUserName = row[UserTable.userID]
                }
                retweetData = Triple(isRetweet, retweetUserName, retweetUserID)
                //end retweet data

                //begin reply data
                val isReply = ReplyTable.select { (ReplyTable.replyID eq tweet.id) }.count() > 0
                var repliedTo: String = ""
                var repliedToID: Int = 0

                if (isReply) {
                    val row = (ReplyTable.join(TweetTable, JoinType.INNER, ReplyTable.originalID, TweetTable.id)
                        .join(UserTable, JoinType.INNER, TweetTable.userID, UserTable.id)
                        .slice(UserTable.userID, ReplyTable.originalID)
                        .select { (ReplyTable.replyID eq tweet.id) }).first()
                    repliedTo = row[UserTable.userID]
                    repliedToID = row[ReplyTable.originalID]
                }
                //basically you cannot favorite/retweet a retweet in MY social media :)
                isRetweeted = !isRetweet && RetweetsTable.select { (RetweetsTable.tweetID eq tweet.id) and (RetweetsTable.userID eq userID)}.count() > 0
                isFavorited = !isRetweet && FavoritesTable.select { (FavoritesTable.tweetID eq tweet.id) and (FavoritesTable.userID eq userID) }.count() > 0

                replyData = Triple(isReply, repliedTo, repliedToID)

                favorites = FavoritesTable.select { FavoritesTable.tweetID eq tweet.id }.count().toInt()
                retweets = RetweetsTable.select { RetweetsTable.tweetID eq tweet.id }.count().toInt()
                userName =
                    UserTable.slice(UserTable.userID).select { UserTable.id eq tweet.userID }.first()[UserTable.userID]
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
    var tweet: Tweet = Tweet(1, 1, "", DateTime.now())
    DatabaseFactory.dbQuery { tweet = Tweet(TweetTable.select { TweetTable.id eq 1 }.first()) }
    println(FullTweet.tweetToFullTweet(tweet, 1))

}