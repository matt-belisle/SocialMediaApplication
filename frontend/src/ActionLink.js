import React from 'react'
export function ActionLink({text, onClick, id}) {
    // i believe doing an anon function each time will actually make a new function each time it is called,
    // not too good for memory
    // also idk if doing things like this is generally considered "good" js, i hate js so this is what i do cause
    // it works
    function onClickHandler(e){
        onClick(e,id)
    }
    return (
        <a href={text} onClick={onClickHandler} style={{paddingLeft: 10}} >
        {text}
        </a>
    );
}