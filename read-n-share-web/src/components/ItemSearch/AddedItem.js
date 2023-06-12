import React from "react";

export const MovieAddedItem = ({results, handleRemoveItem}) => {
    return (
        <div className="row">
            {results.map((result) => (
                <div key={result.imdbId} className="col-md-4 mb-4">
                    <div className="card">
                        <img src={result.image} alt={result.title} className="card-img-top"/>
                        <div className="card-body">
                            <h4 className="card-title">{result.title}</h4>
                            <p className="card-text">{result.description}</p>
                            <button
                                className="btn brn-danger"
                                onClick={(event) => {
                                    event.preventDefault();
                                    handleRemoveItem(result.imdbId);
                                }}
                            >
                                Remove
                            </button>
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );
};

export const BookAddedItem = ({results, handleRemoveItem}) => {
    return (
        <div className="row">
            {results.map((result) => (
                <div key={result.goggleBooksId} className="col-md-4 mb-4">
                    <div className="card">
                        <img src={result.image} alt={result.title} className="card-img-top"/>
                        <div className="card-body">
                            <h4 className="card-title">{result.title}</h4>
                            <p className="card-text">{result.description}</p>
                            <button
                                className="btn btn-primary"
                                onClick={(event) => {
                                    event.preventDefault();
                                    handleRemoveItem(result.goggleBooksId);
                                }}
                            >
                                Remove
                            </button>
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );
};


export default {MovieAddedItem, BookAddedItem}