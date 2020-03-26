import React, {Component} from 'react';
import Tweet from "./Tweet";

class App extends Component {

    state = {
        contacts: []
    };

    componentDidMount() {
        fetch('http://localhost:8080/tweets/ForUser/1')
            .then(res => res.json())
            .then((data) => {
                this.setState({contacts: data})
            })
            .catch(console.log)
    }

    render() {
        return (
            <Tweet contacts={this.state.contacts}/>
        )
    }
}

export default App;