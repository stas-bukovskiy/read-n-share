import React from 'react';
import './FloatingActionButton.css';

const FloatingActionButton = ({onClick}) => {
    return (
        <button className="fab" onClick={onClick}>
            <span className="plus">+</span>
        </button>
    );
};

export default FloatingActionButton;
