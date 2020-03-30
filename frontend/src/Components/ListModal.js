import React, {useState} from "react";
import {Button, ListGroup, ListGroupItem} from "react-bootstrap";
import Modal from "react-bootstrap/Modal";

//a list of users modal
const ListModal = ({getListLink, title, linkText}) => {
    let [list, setList] = useState([]);
    const [show, setShow] = useState(false);

    const handleClose = () => setShow(false);
    const handleShow = () => {
        fetch(getListLink).then((res => res.json())).then((res) => {
                setList(res);
                setShow(true);
            }
        );
    };

    return (
        <>
            <Button variant="link" onClick={handleShow}>
                {linkText}
            </Button>

            <Modal show={show} onHide={handleClose}>
                <Modal.Header closeButton>
                    <Modal.Title>{title}</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <ListGroup>
                        {list.map((user) => <ListGroupItem>{user.userID}</ListGroupItem>)}
                    </ListGroup>
                </Modal.Body>
                <Modal.Footer>

                </Modal.Footer>
            </Modal>
        </>
    );
};

export default ListModal