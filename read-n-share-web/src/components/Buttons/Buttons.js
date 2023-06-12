import React from 'react';

const NextButton = ({onClick, disabled}) => (
    <button className="btn btn-primary btn-lg" onClick={onClick} disabled={disabled}>
        Next <i className="fas fa-arrow-right"></i>
    </button>
);

const BackButton = ({onClick, disabled}) => (
    <button className="btn btn-primary btn-lg" onClick={onClick}>
        <i className="fas fa-arrow-left" disabled={disabled}></i> Back
    </button>
);

export {NextButton, BackButton};
