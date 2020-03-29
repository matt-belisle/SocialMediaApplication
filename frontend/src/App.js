import React, {Component} from 'react';
import Tweet from "./Components/Tweet";
import {ToastProvider} from 'react-toast-notifications'
import Login from "./Components/Login";

class App extends Component {


    constructor() {
        super();
        this.getTweets = this.getTweets.bind(this)
        this.getUser = this.getUser.bind(this)
        this.state = {
            tweets: [],
            screen: AppState.LOGIN,
            loggedIn: {},
            users: []
        }
    };

    getUser(userName) {
        fetch(`http://localhost:8080/user/${userName}`).then(res => res.json()).then((data) => {
            this.setState({loggedIn: data})
            this.getTweets(this.state.loggedIn.id)
        }).catch(console.log)
        this.setState({screen: AppState.TWEETS})

        fetch(`http://localhost:8080/users/all`).then(res => res.json()).then((data) => {
            this.setState({users: data})
        }).catch(console.log)
    }

    getTweets(userID) {
        fetch(`http://localhost:8080/tweets/ByFollowed/${userID}`)
            .then(res => res.json())
            .then((data) => {
                this.setState({tweets: data})
            })
            .catch(console.log)
    }

    render() {
        let currScreen;
        switch (this.state.screen) {
            case AppState.LOGIN:
                currScreen = <Login getUserAndFinishLogin={this.getUser}/>;
                break;
            case AppState.TWEETS:
                currScreen =
                    <Tweet tweets={this.state.tweets} header={`Tweets by People You Follow`}
                           currUserID={this.state.loggedIn.id} refreshTweets={this.getTweets} users={this.state.users}/>
                break;
            // if anything magically breaks just boot back to login
            default:
                currScreen = <Login/>;
                break;
        }
        return (
            <ToastProvider>
                {currScreen}
                {/*<Tweet tweets={this.state.tweets} header={"Tweets For User 1"} currUserID={1} refreshTweets={this.getTweets}/>*/}
            </ToastProvider>
        )
    }
}

export const AppState = {
    LOGIN: 'login',
    TWEETS: 'tweet'
};
export default App;