import React from "react";

export const MovieSearchResultComponent = ({results, handleAddItem}) => {
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
                                className="btn btn-primary"
                                onClick={(event) => {
                                    event.preventDefault();
                                    handleAddItem(result.imdbId, {...result, itemId: result.imdbId});
                                }}
                            >
                                Add to Wishlist
                            </button>
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );
};

export const BookSearchResultComponent = ({results, handleAddItem}) => {
    console.log(results)
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
                                    handleAddItem(result.goggleBooksId, {...result, itemId: result.goggleBooksId});
                                }}
                            >
                                Add to Wishlist
                            </button>
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );
};


export default {MovieSearchResultComponent, BookSearchResultComponent}