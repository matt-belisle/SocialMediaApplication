import React, {Component} from 'react';
import Tweet from "./Tweet";

class App extends Component {


    constructor() {
        super();
        this.getTweets = this.getTweets.bind(this)
        this.getTweets.bind(this)
        this.state = {
            tweets: []
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
        return (
            <Tweet tweets={this.state.tweets} header={"Tweets For User 1"} currUserID={1} refreshTweets={this.getTweets}/>
        )
    }
}

export default App;