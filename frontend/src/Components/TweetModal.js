import React, {useState} from "react";
import Popup from "reactjs-popup";
import {Button, FormControl} from "react-bootstrap";
import InputGroup from "react-bootstrap/InputGroup";

const TweetModal = ({isReply, replyToID, currUserID, refreshTweets}) => {
    // a tweet will consist of a text box, and two buttons either tweet or cancel, but the "tweet button" content is configurable
    // the given submit function will submit back to whatever created it

    let [textBoxState, setTextBoxState] = useState("")


    function submit(close) {
        let string = `http://localhost:8080/tweet/${currUserID}${isReply ? `/${replyToID}` : ""}`
        fetch(string, {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }, body: JSON.stringify({tweet: textBoxState})
        }).then(res => {
            if (res.ok) {
                refreshTweets(currUserID)
            }
        })

    }

    const text = isReply ? "Reply" : "Tweet";

    return (
        <Popup
            trigger={<Button className="button" variant="link"> {text} </Button>}
            modal
            closeOnDocumentClick
            closeOnEscape
        >
            {close => (
                <div>
                    <InputGroup>
                        <InputGroup.Prepend>
                            <InputGroup.Text>{text} Content</InputGroup.Text>
                        </InputGroup.Prepend>
                        <FormControl as="textarea" aria-label="With textarea"
                                     onChange={e => setTextBoxState(e.target.value)}/>
                    </InputGroup>
                    <InputGroup className="mb-3">
                        <div>

                            <Button bsSize="large" disabled={textBoxState.trim().length === 0} variant={"dark"}
                                    style={{marginRight: 10}}
                                    onClick={(e) => {
                                        e.preventDefault()
                                        submit(textBoxState, isReply);
                                        close();
                                    }}>
                                {text}!
                            </Button>
                            <Button bsSize="large" disabled={false} variant={"dark"} onClick={() => {
                                close()
                            }}>
                                Cancel
                            </Button>
                        </div>
                    </InputGroup>
                </div>
            )}
        </Popup>
    )
};

export default TweetModal