const BookSearchResultComponent = ({results, handleAddItem}) => {
    return (
        <div>
            {results.map((result) => (
                <div key={result.goggleBooksId}>
                    <img src={result.imageLinks.thumbnail} alt={result.title}/>
                    <h4>{result.title}</h4>
                    <p>{result.description}</p>
                    <button onClick={(event) => {
                        event.preventDefault();
                        handleAddItem(result.goggleBooksId)
                    }}>Add to Wishlist
                    </button>
                </div>
            ))}
        </div>
    )
};

export default BookSearchResultComponent;