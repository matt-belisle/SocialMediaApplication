import React, {Component} from 'react';
import Tweet from "./Components/Tweet";
import {ToastProvider} from 'react-toast-notifications'
import Login from "./Components/Login";
import UserProfile from "./Components/UserProfile";
import Ribbon from "./Components/Ribbon";
export const AppState = {
    LOGIN: 'login',
    TWEETS: 'tweet',
    VIEW_REPLIES: 'viewReplies',
    HASHTAG: 'hashtag',
    PROFILE: 'profile'
};
class App extends Component {
    constructor() {
        super();
        this.getTweets = this.getTweets.bind(this)
        this.getUser = this.getUser.bind(this)
        this.replyChain = this.replyChain.bind(this)
        this.deleteTweet = this.deleteTweet.bind(this)
        this.getTweetsForHashtag = this.getTweetsForHashtag.bind(this)
        this.hashTagRefresh = this.hashTagRefresh.bind(this)
        this.selectUser = this.selectUser.bind(this)
        this.logout = this.logout.bind(this)
        this.profile = this.profile.bind(this)
        this.home = this.home.bind(this)
        this.state = this.defaultState
    }
    defaultState = {
        tweets: [],
        screen: AppState.LOGIN,
        loggedIn: {},
        selectedUser: {},
        users: [],
        selectedTweet: {},
        selectedReply: {},
        searchedHashTag: ""
    };

    getUser(userName) {
        fetch(`http://localhost:8080/user/${userName}`).then(res => res.json()).then((data) => {
            this.setState({loggedIn: data})
            this.getTweets(this.state.loggedIn.id)
            this.setState({screen: AppState.TWEETS})
        }).catch(console.log)


        fetch(`http://localhost:8080/users/all`).then(res => res.json()).then((data) => {
            this.setState({users: data})
        }).catch(console.log)
    }

    getTweets(userID) {
        if(this.state.screen === AppState.VIEW_REPLIES && this.state.tweets.length > 0){
            this.replyChain(this.state.tweets[0])
        } else if (this.state.screen === AppState.PROFILE){
            fetch(`http://localhost:8080/tweets/ForUser/${userID}/${this.state.selectedUser.id}`)
                .then(res => res.json())
                .then((data) => {
                    this.setState({tweets: data})
                })
                .catch(console.log)
        } else {
            fetch(`http://localhost:8080/tweets/ByFollowed/${userID}`)
                .then(res => res.json())
                .then((data) => {
                    this.setState({tweets: data})
                })
                .catch(console.log)
        }
    }

    getTweetsForHashtag(hashtag, subHashTag){
        fetch(`http://localhost:8080/hashtag/${this.state.loggedIn.id}/${encodeURI(hashtag).replace(/#/g, "%23")}/${subHashTag}`).then(res => res.json())
            .then((data) => {
                this.setState({tweets: data, screen: AppState.HASHTAG, searchedHashTag: hashtag})
            })
            .catch(console.log)
    }
    // called for the hashtag clicked screen
    hashTagRefresh(userID) {
        this.getTweetsForHashtag(this.state.searchedHashTag, true)
    }
    // will effectively be the view more button for reply chaining
    replyChain(tweet) {
        fetch(`http://localhost:8080/tweets/replyChain/${tweet.tweetID}/${this.state.loggedIn.id}`)
            .then(res => res.json())
            .then((data) => {
                this.setState({tweets: data, screen: AppState.VIEW_REPLIES, selectedReply: tweet})
            })
            .catch(console.log)
    }

    deleteTweet(tweet) {
        if(tweet.userID === this.state.loggedIn.id){
            fetch(`http://localhost:8080/deleteTweet/${tweet.tweetID}`, {method: 'delete'}).then( ret => {
                    if (this.state.screen === AppState.VIEW_REPLIES) {
                        if (this.state.selectedReply.tweetID === tweet.tweetID) {
                            this.setState({screen: AppState.TWEETS})
                            this.getTweets(this.state.loggedIn.id)
                        } else {
                            this.replyChain(this.state.selectedReply)
                        }
                    } else if(this.state.screen === AppState.TWEETS) {
                        this.getTweets(this.state.loggedIn.id)
                    }
                }
            )
        } else {
            console.log("Tried to delete a tweet you do not own")
        }
    }
    logout(e) {
        e.preventDefault();
        this.setState(this.defaultState )
    }
    home(e) {
        e.preventDefault();
        fetch(`http://localhost:8080/tweets/ByFollowed/${this.state.loggedIn.id}`)
            .then(res => res.json())
            .then((data) => {
                this.setState({screen: AppState.TWEETS, tweets: data})
            })
            .catch(console.log)
    }
    profile(e){
        e.preventDefault();
        fetch(`http://localhost:8080/tweets/ForUser/${this.state.loggedIn.id}/${this.state.loggedIn.id}`)
            .then(res => res.json())
            .then((data) => {
                this.setState({screen: AppState.PROFILE, tweets: data, selectedUser: this.state.loggedIn})
            })
            .catch(console.log);
    }
    // transitions to a usersProfile
    selectUser(userName){
        fetch(`http://localhost:8080/user/${userName}`).then(res => res.json()).then((data) => {
                this.setState({screen: AppState.PROFILE, selectedUser: data})
                this.getTweets(this.state.loggedIn.id)
            }
        )
    }

    render() {
        let tweetScreen = "";
        let profileScreen = "";
        let loginScreen = "";
        let tweetHeader = "";
        let refreshTweets = "";
        let currScreen = "";
        let ribbon = "";
        switch (this.state.screen) {
            case AppState.LOGIN:
                loginScreen = <Login getUserAndFinishLogin={this.getUser}/>;
                break;
            case AppState.TWEETS:
                refreshTweets = this.getTweets;
                tweetHeader = `Tweets by People You Follow`;
                break;
            case AppState.VIEW_REPLIES:
                refreshTweets=this.getTweets;
                tweetHeader = `Reply Chain For Tweet #${this.state.selectedReply.tweetID}`;
                break;
            case AppState.HASHTAG:
                refreshTweets=this.hashTagRefresh;
                tweetHeader = `Tweets for HashTag #${this.state.searchedHashTag}`;
                break;
            case AppState.PROFILE:
                refreshTweets = this.getTweets;
                tweetHeader = `Tweets by @${this.state.selectedUser.userID}`;
                break;
            // if anything magically breaks just boot back to login
            default:
                currScreen = <Login getUserAndFinishLogin={this.getUser}/>;
                break;

        }
        ribbon = <Ribbon refreshTweets={refreshTweets} currUser={this.state.loggedIn} logOutCallback={this.logout} homeCallback={this.home} profileCallBack={this.profile} />
        profileScreen = this.state.screen === AppState.PROFILE ? <UserProfile currentUser={this.state.loggedIn} viewedUser={this.state.selectedUser} fetchUser={this.selectUser}/> : ""
        tweetScreen = <Tweet tweets={this.state.tweets} header={tweetHeader} currUserID={this.state.loggedIn.id} refreshTweets={refreshTweets} deleteTweet={this.deleteTweet} searchHashTag={this.getTweetsForHashtag} selectUser={this.selectUser} replyChain={this.replyChain} users={this.state.users} />
        currScreen = this.state.screen === AppState.LOGIN ? loginScreen : <div>{ribbon} {profileScreen} {tweetScreen}</div>

        return (
            <ToastProvider>
                {currScreen}
            </ToastProvider>
        )
    }
}


export default App;