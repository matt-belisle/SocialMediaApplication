import React from 'react'
import {ActionLink} from "./ActionLink";
import TweetModal from "./TweetModal";
import ListModal from "./ListModal";
import {linkString} from "../Configuration";

const reactStringReplace = require('react-string-replace');

const Tweet = ({tweets, header, currUserID, refreshTweets, users, replyChain, deleteTweet, searchHashTag, selectUser}) => {

    function handleFavoriteClick(e, tweet) {
        e.preventDefault();
        let method = tweet.isFavorited === true ? 'delete' : 'post';
        fetch(`http://${linkString}/favorite/tweet/${tweet.tweetID}/${currUserID}`, {method: method}).then (() => refreshTweets(currUserID))
        //refresh the tweet
    }

    function handleRetweetClick(e, tweet) {
        e.preventDefault();
        let method = tweet.isRetweeted === true ? 'delete' : 'post';
        fetch(`http://${linkString}/retweet/tweet/${tweet.tweetID}/${currUserID}`, {method: method}).then(() => refreshTweets(currUserID))
    }

    // should reload the page with all tweets with the hashtag
    function handleHashtagClick(e, hashtag) {
        e.preventDefault()
        // go to the parent and tell it we need to get these tweets
        searchHashTag(hashtag, true)
    }

    // //should reload the page with the clicked users profile
    // function handleMentionClick(e, mention) {
    //
    // }
    //
    // //should be same as mention so collapse?
    function handleUserClick(e, userName) {
        e.preventDefault();
        // transition to usersProfile
        selectUser(userName)
    }

    //view more of replies
    function handleViewMoreClick(e, tweet) {
        e.preventDefault()
        replyChain(tweet)
    }

    function handleDeleteClick(e, tweet) {
        e.preventDefault()
        deleteTweet(tweet)
    }


    return (

        <div>
            <center><h1>{header}</h1></center>
            {tweets.map((tweet) => {
                //users
                let replacedText = reactStringReplace(tweet.text, /@(\w+)/g, (match, i) => {
                    if (users.indexOf(match) >= 0) {
                        return <a key={match + i} href={`https://twitter.com/${match}`}>@{match}</a>
                    } else {
                        return `@${match}`
                    }
                });

                // Match hashtags
                replacedText = reactStringReplace(replacedText, /#([\w(##)]+)/g, (match, i) => (
                    <ActionLink text={`#${match}`} id={match} onClick={handleHashtagClick}/>
                ));

                let retweetData = tweet.isRetweet ?
                    <h6 className="card-subtitle mb-2 text-muted">{`${tweet.user_name} retweeted ${tweet.originalPoster}`}</h6> : ""

                let replyData = tweet.isReply ?
                    <h6 className="card-subtitle mb-2 text-muted">{`${tweet.user_name} replied to ${tweet.repliedTo} -- ${tweet.repliedToID}`}
                        <ActionLink text={"View Reply Chain"} onClick={handleViewMoreClick} id={tweet}/></h6> : ""
                let deleteTweet = tweet.originalPosterID === currUserID ?
                    <ActionLink text={"Delete"} onClick={handleDeleteClick} id={tweet}/> : ""

                return (<div key={tweet.tweetID.toString()} className="card">
                    <div className="card-body">
                        <div style={{paddingBottom: '10px'}}>
                            <h5 className="card-title"><ActionLink text={`@${tweet.user_name}`} onClick={handleUserClick} id={tweet.user_name}/> -- {tweet.tweetID}</h5>
                            {retweetData}
                            {replyData}
                        </div>
                        <h6 className="card-subtitle mb-2 text-muted">{replacedText}</h6>
                        <p className="card-text">{}</p>
                        <div>
                            <ActionLink text={tweet.isFavorited === true ? "UnFavorite" : "Favorite"}
                                        onClick={handleFavoriteClick} id={tweet}/>
                            <span>: {tweet.favorites}</span>
                            <ActionLink text={tweet.isRetweeted === true ? "UnRetweet" : "Retweet"}
                                        onClick={handleRetweetClick} id={tweet}/>
                            <span>: {tweet.retweets}</span>
                            <TweetModal replyToID={tweet.tweetID.toString()} currUserID={currUserID} isReply={true}
                                        refreshTweets={refreshTweets}/>
                            {deleteTweet}
                            {/*the ternary is effectively conditional display*/}
                            {tweet.favorites > 0 ? <ListModal linkText={"View Favorites"}
                                                              getListLink={`http://${linkString}/favorite/tweet/${tweet.tweetID}`}
                                                              title={`People who have favorited Tweet #${tweet.tweetID}`}/> : ""}
                            {tweet.retweets > 0 ? <ListModal linkText={"View Retweets"}
                                                             getListLink={`http://${linkString}/retweet/tweet/${tweet.tweetID}`}
                                                             title={`People who have retweeted Tweet #${tweet.tweetID}`}/> : ""}

                        </div>
                    </div>
                </div>)
            })}
        </div>
    )
};

export default Tweet