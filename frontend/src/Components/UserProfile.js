// a user profile consists of 2 columns, 1 containing user data (their follow count, followers count, description and username)
// and the other containing their tweets/retweets

//a list of users modal
import React, {useEffect, useState} from "react";
import {Button} from "react-bootstrap";
import Form from "react-bootstrap/Form";
import ListModal from "./ListModal";
import {ActionLink} from "./ActionLink";

const UserProfile = ({currentUser, viewedUser, fetchUser}) => {
    let [textArea, setTextArea] = useState(viewedUser.description);
    let [followCount, setFollowCount] = useState(0)
    let [followersCount, setFollowersCount] = useState(0)
    let [follows, setFollows] = useState()

    function submit() {
        fetch(`http://localhost:8080/user/description/${viewedUser.id}`, {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }, body: JSON.stringify({description: textArea})
        }).then(res => {
            if (res.ok) {
                fetchUser(viewedUser.userID)
            }
        })
    }
    function followCountFetch(link, setX) {
        fetch(`http://localhost:8080/count/${link}`).then(res => res.json()).then(res => setX(res))
    }
    // a button handler
    function assignFollows(e, id){
        e.preventDefault();
        let method = follows ? 'delete' : 'post';
        fetch(`http://localhost:8080/follow/${currentUser.id}/${viewedUser.id}`, {method: method}).then(() => getFollows())
    }

    function getFollows(){
        fetch(`http://localhost:8080/follow/${currentUser.id}/${viewedUser.id}`).then(res => res.json()).then(res => setFollows(res))
    }
    // only call when mounted or if the user follows/unfollows them
    useEffect(() =>{
        async function fetchData() {
            followCountFetch(`Followers/${viewedUser.id}`, setFollowersCount)
            followCountFetch(`Following/${viewedUser.id}`, setFollowCount)
            getFollows()
        }
        fetchData()
    },[follows, viewedUser.id])


    return (
        <><Form>
            <div style={{paddingBottom: '10px'}}>
                <h5 className="card-title"> {`@${viewedUser.userID}'s Profile`}</h5>
            </div>

            <Form.Group controlId="exampleForm.ControlTextarea1">
                <Form.Label>Description</Form.Label>
                <Form.Control as="textarea" rows="3" value={textArea} readOnly={currentUser.id !== viewedUser.id} onChange={(e) => setTextArea(e.target.value) } />
            </Form.Group>
            {currentUser.id === viewedUser.id ? <Button variant="dark" type="button" disabled={textArea === viewedUser.description} onClick={(e) => {
                e.preventDefault()
                submit();
            }} >
                Change Description
            </Button>: ""}
            <div>
                <ListModal getListLink={`http://localhost:8080/Following/${viewedUser.id}`} title={`${viewedUser.userID} is Following`} linkText={"Following"} />
                <span>{followCount}</span>
                <ListModal getListLink={`http://localhost:8080/Followers/${viewedUser.id}`} title={`Followers for user ${viewedUser.userID}`} linkText={"Followers"}/>
                <span>{followersCount}</span>
                {currentUser.id !== viewedUser.id ? <ActionLink onClick={assignFollows} text={follows ? "UnFollow" : "Follow"} />: ""}
            </div>
        </Form>
            <hr/>
        </>
    );
};

export default UserProfile