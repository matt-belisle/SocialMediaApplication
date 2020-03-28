import React, {Component} from 'react';
import Tweet from "./Components/Tweet";
import { ToastProvider } from 'react-toast-notifications'
import Login from "./Components/Login";
class App extends Component {


    constructor() {
        super();
        this.getTweets = this.getTweets.bind(this)
        this.getTweets.bind(this)
        this.state = {
            tweets: [],
            screen: AppState.LOGIN
        };
    }
    getTweets(userID){
        fetch(`http://localhost:8080/tweets/ForUser/${userID}`)
            .then(res => res.json())
            .then((data) => {
                this.setState({tweets: data})
            })
            .catch(console.log)
    }
    componentDidMount() {

        this.getTweets(1)
    }

    render() {
        let currScreen;
        switch(this.state.screen) {
            case AppState.LOGIN:
                currScreen = <Login/>;
                break;
                // if anything breaks just boot back to login
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