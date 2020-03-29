import React, {useState} from 'react'
import {ActionLink} from "./ActionLink";
import Popup from "reactjs-popup";

const reactStringReplace = require('react-string-replace');

const Tweet = ({tweets, header, currUserID, refreshTweets, users}) => {

    function handleFavoriteClick(e, tweet) {
        e.preventDefault();
        let method = tweet.isFavorited === true ? 'delete' : 'post';
        fetch(`http://localhost:8080/favorite/tweet/${tweet.tweetID}/${currUserID}`, {method: method})
        //refresh the tweet
        refreshTweets(currUserID)
    }

    function handleRetweetClick(e, tweet) {
        e.preventDefault();
        let method = tweet.isRetweeted === true ? 'delete' : 'post';
        fetch(`http://localhost:8080/retweet/tweet/${tweet.tweetID}/${currUserID}`, {method: method})
        //refresh the tweet
        refreshTweets(currUserID)
    }

    // should reload the page with all tweets with the hashtag
    function handleHashtagClick(e, hashtag) {

    }

    //should reload the page with the clicked users profile
    function handleMentionClick(e, mention) {

    }

    // should effectively load whatever will be the thing that makes tweets
    function handleReplyClick(e, tweetRepliedTo) {
        e.preventDefault();
    }

    //should be same as mention so collapse?
    function handleUserClick(e, userID) {

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
                replacedText = reactStringReplace(replacedText, /#(\w+)/g, (match, i) => (
                    <a key={match + i} href={`https://twitter.com/hashtag/${match}`}>#{match}</a>
                ));

                let retweetData = tweet.isRetweet ?
                    <h6 className="card-subtitle mb-2 text-muted">{`${tweet.user_name} retweeted ${tweet.originalPoster}`}</h6> : ""

                return (<div key={tweet.tweetID.toString()} className="card">
                    <div className="card-body">
                        <h5 className="card-title">{tweet.user_name}</h5>
                        {retweetData}
                        <h6 className="card-subtitle mb-2 text-muted">{replacedText}</h6>
                        <p className="card-text">{}</p>
                        <div>
                            <ActionLink text={tweet.isFavorited === true ? "UnFavorite" : "Favorite"}
                                        onClick={handleFavoriteClick} id={tweet}/>
                            <span>: {tweet.favorites}</span>
                            <ActionLink text={tweet.isRetweeted === true ? "UnRetweet" : "Retweet"}
                                        onClick={handleRetweetClick} id={tweet}/>
                            <span>: {tweet.retweets}</span>
                        </div>
                    </div>
                </div>)
            })}
        </div>
    )
};

export default Tweet