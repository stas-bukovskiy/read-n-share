import React from "react";
import {Link} from "react-router-dom";
import "../../css/styles.css"

export const MovieCard = ({movieData}) => {
    return (
        <div key={movieData.imdbId} className="col-md-4 mb-4 animated-card">
            <Link to={`/movies/${movieData.imdbId}`} className="text-decoration-none">
                <div className="card">
                    <img src={movieData.imageURL} alt={movieData.title} className="card-img-top"/>
                    <div className="card-body">
                        <h4 className="card-title">{movieData.title}</h4>
                        <p className="card-text">{movieData.plot}</p>
                    </div>
                </div>
            </Link>
        </div>
    );
};

export default MovieCard;