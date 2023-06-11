const MovieSearchResultComponent = ({results, handleAddItem}) => {
    return (
        <div>
            {results.map((result) => (
                <div key={result.imdbId}>
                    <img src={result.image} alt={result.title} width="300px" height="auto"/>
                    <h4>{result.title}</h4>
                    <p>{result.description}</p>
                    <button onClick={(event) => {
                        event.preventDefault();
                        handleAddItem(result.imdbId)
                    }}>Add to Wishlist
                    </button>
                </div>
            ))}
        </div>
    )
};

export default MovieSearchResultComponent;