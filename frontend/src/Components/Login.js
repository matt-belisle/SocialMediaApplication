// this is gonna be the simplest of all login pages

import React, {useState} from "react";
import {useToasts} from 'react-toast-notifications'
import {Button, FormGroup, FormControl, FormLabel} from "react-bootstrap";
import TweetModal from "./TweetModal";
import InputGroup from "react-bootstrap/InputGroup";

export default function Login({getUserAndFinishLogin}) {
    const {addToast} = useToasts();
    const [user, setUser] = useState("");
    function validateForm() {
        // maximum user name is 32 characters long
        return user.match("[A-Za-z0-9_]+") && user.length < 33;
    }

    // the enter key is just gonna log you in
    function handleKeyPress(target) {
        target.preventDefault()
        if(target.charCode === 13){
            handleLogin(target)
        }
    }

    function handleLogin(event) {
        event.preventDefault()
        // hit the login endpoint, effectively just sees whether a user exists
        fetch(`http://localhost:8080/login/${user}`, {method: "post"}).then(res => {
            if (res.ok) {
                addToast(`Successfully logged in as ${user}`, {
                    appearance: 'success'
                })
                getUserAndFinishLogin(user)
            } else {
                addToast(`No user with the user name ${user} exists please try again`, {
                    appearance: 'error'
                })
            }
        })
    }

    function handleRegister(event) {
        event.preventDefault()
        fetch(`http://localhost:8080/user/${user}`, {method: "post"}).then(res => {
            if (res.ok) {
                addToast(`Successfully registered user: ${user}`, {
                    appearance: 'success'
                })
                getUserAndFinishLogin(user)
            } else {
                addToast(`A user with the user name ${user} already exists please register again`, {
                    appearance: 'error'
                })
            }
        })
    }

    return (

        <div className="Login" style={{ "display": "flex",
            "justify-content": "center",
            "align-items": "center", 'height': '100%'}}>
            <form style={{"text-align": 'center', 'width': '100%'}}>

                <TweetModal isReply={false} currUserID={1} replyToID={-1}/>
                <div style={{width: "90%", display: "inline-block" }}>
                <FormGroup controlId="user" bsSize="large">
                    <FormLabel>Login To Social Media</FormLabel>
                    <InputGroup className="mb-3">
                        <InputGroup.Prepend>
                            <InputGroup.Text id="basic-addon1">@</InputGroup.Text>
                        </InputGroup.Prepend>
                        <FormControl
                            placeholder="User Name"
                            aria-label="User Name"
                            aria-describedby="basic-addon1"
                            onChange={e => setUser(e.target.value)}
                            onKeyPress={handleKeyPress}

                        />
                    </InputGroup>
                </FormGroup>

                <Button block bsSize="large" type={"submit"} disabled={!validateForm()} onClick={handleLogin} variant={"dark"} >
                    Login
                </Button>
                <div>Haven't made an account?</div>
                <Button block bsSize="large" disabled={!validateForm()} onClick={handleRegister} variant={"dark"}>
                    Register
                </Button>
                </div>
            </form>
        </div>
    );
}