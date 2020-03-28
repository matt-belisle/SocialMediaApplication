// this is gonna be the simplest of all login pages

import React, {useState} from "react";
import {useToasts} from 'react-toast-notifications'
import {Button, FormGroup, FormControl, FormLabel} from "react-bootstrap";

export default function Login(props) {
    const {addToast} = useToasts()
    const [user, setUser] = useState("");

    function validateForm() {
        // maximum user name is 32 characters long
        return user.match("[A-Za-z0-9]") && user.length < 33;
    }

    function handleLogin(event) {
        // hit the login endpoint, effectively just sees whether a user exists
        fetch(`http://localhost:8080/login/${user}`, {method: "post"}).then(res => {
            if (res.ok) {
                addToast(`Successfully logged in as ${user}`, {
                    appearance: 'success',
                    position: 'bottom-right'
                })
            } else {
                addToast(`No user with the user name ${user} exists please try again`, {
                    appearance: 'error',
                    position: 'bottom-right'
                })
            }
        })
    }

    function handleRegister(event) {
        fetch(`http://localhost:8080/user/${user}`, {method: "post"}).then(res => {
            if (res.ok) {
                addToast(`Successfully registered user: ${user}`, {
                    appearance: 'success',
                    position: 'bottom-right'
                })
            } else {
                addToast(`A user with the user name ${user} already exists please register again`, {
                    appearance: 'error',
                    position: 'bottom-right'
                })
            }
        })
    }

    return (

        <div className="Login">
            <form>
                <FormGroup controlId="user" bsSize="large">
                    <FormLabel>User Name</FormLabel>
                    <FormControl
                        autoFocus
                        type="user"
                        value={user}
                        onChange={e => setUser(e.target.value)}
                    />
                </FormGroup>
                <Button block bsSize="large" disabled={!validateForm()} onClick={handleLogin}>
                    Login
                </Button>
                <span>Haven't made an account?</span>
                <Button block bsSize="large" disabled={!validateForm()} onClick={handleRegister}>
                    Register
                </Button>
            </form>
        </div>
    );
}