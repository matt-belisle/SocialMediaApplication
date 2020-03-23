import org.joda.time.DateTime


// a Full tweet consists of the regular tweet metadata
// as well as if it is a retweet the original poster
// and finally the favorites and retweets
data class FullTweet(
    val text: String,
    val userID: Int,
    val user_name: String,
    val timestamp: DateTime,
    val isRetweet: Boolean,
    val originalPoster: String,
    val favorites: Int,
    val retweets: Int
)