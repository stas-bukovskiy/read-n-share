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
                            <div className="d-flex justify-content-center">
                                <button
                                    className="btn btn-outline-danger"
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
                </div>
            ))}
        </div>
    );
};

export const BookAddedItem = ({results, handleRemoveItem}) => {
    return (
        <div className="row">
            {results.map((result) => (
                <div key={result.imdbId} className="col-md-4 mb-4">

                    <div className="card">
                        <img src={result.imageLinks.smallThumbnail || result.imageLinks.thumbnail} alt={result.title}
                             className="card-img-top"/>
                        <div className="card-body">
                            <h4 className="card-title">{result.title}</h4>
                            <p className="card-text">{result.subtitle}</p>
                            <div className="d-flex justify-content-center">

                                <button
                                    className="btn btn-outline-danger"
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
                </div>
            ))}
        </div>
    );
};


export default {MovieAddedItem, BookAddedItem}