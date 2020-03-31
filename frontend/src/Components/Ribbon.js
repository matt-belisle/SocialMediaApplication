import React from "react";
import TweetModal from "./TweetModal";
import {ActionLink} from "./ActionLink";

//a list of users modal
const Ribbon = ({logOutCallback, profileCallBack, homeCallback, currUser, refreshTweets}) => {

    return (
        <div style={{backgroundColor: "#212529"}}>
            <ActionLink onClick={homeCallback} id={currUser} text={"Home"} />
            <ActionLink onClick={profileCallBack} id={currUser} text={"My Profile"} />
            <TweetModal isReply={false} refreshTweets={refreshTweets} currUserID={currUser.id} />
            <ActionLink onClick={logOutCallback} id={currUser} text={"Logout"} />
            </div>
    );
};

export default Ribbon