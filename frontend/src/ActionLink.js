import React from 'react'
export function ActionLink({text, onClick}) {

    return (
        <a href="favorite" onClick={onClick} >
        {text}
        </a>
    );
}