import React from 'react'
import {ActionLink} from "./ActionLink";

const Tweet = ({ contacts }) => {

    function handleFavoriteClick(e) {
        e.preventDefault()
        console.log("favorite this tweet")
    }

    function handleRetweetClick(e) {
        e.preventDefault()
        console.log("Retweet this tweet")
    }

    return (
        <div>
            <center><h1>Tweets</h1></center>
            {contacts.map((tweet) => (
                <div className="card">
                    <div className="card-body">
                        <h5 className="card-title">{tweet.user_name}</h5>
                        <h6 className="card-subtitle mb-2 text-muted">{tweet.tweetID}</h6>
                        <p className="card-text">{tweet.text}</p>
                            <ActionLink text="Favorite" onClick={handleFavoriteClick}/>
                    </div>
                </div>
            ))}
        </div>
    )
};

export default Tweet