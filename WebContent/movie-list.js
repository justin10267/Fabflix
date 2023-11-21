function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");
    let movieTableBodyElement = jQuery("#movie_table_body").empty();
    resultData.forEach(movie => {
        let genresHtml = movie["movie_genres"].split(",").map(genre => `<a href="list.html?genre=${encodeURIComponent(genre.trim())}">${genre.trim()}</a>`).join(", ");
        let starsHtml = movie["movie_stars"].split(",").map(star => {
            let [id, name] = star.split(":");
            return `<a href="single-star.html?id=${id.trim()}">${name.trim()}</a>`;
        }).join(", ");

        let rowHTML = `<tr>
            <th><a href="single-movie.html?id=${movie['movie_id']}">${movie['movie_title']}</a></th>
            <th>${movie["movie_year"]}</th>
            <th>${movie["movie_director"]}</th>
            <th>${genresHtml}</th>
            <th>${starsHtml}</th>
            <th>${movie["movie_rating"]}</th>
        </tr>`;
        movieTableBodyElement.append(rowHTML);
    });
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/movies",
    success: handleMovieResult
});

function setupAutocomplete() {
    const storage = window.sessionStorage;
    $('#autocomplete').autocomplete({
        lookup: function (query, doneCallback) {
            handleLookup(query, doneCallback, storage);
        },
        onSelect: handleSelectSuggestion,
        deferRequestBy: 300,
        minChars: 3,
        triggerSelectOnValidInput: false
    });
    $('#autocomplete').keypress(function(event) {
        if (event.keyCode === 13) { // Enter key
            handleNormalSearch($('#autocomplete').val());
        }
    });
    document.getElementById("autocompleteSearchButton").addEventListener("click", () => {
        handleNormalSearch($('#autocomplete').val());
    });
}

function handleLookup(query, doneCallback, storage) {
    console.log("autocomplete initiated");
    let lowerCaseQuery = query.toLowerCase();
    if (storage.getItem(lowerCaseQuery)) {
        console.log("using autocompleteCache for lookup");
        doneCallback({ suggestions: JSON.parse(storage.getItem(lowerCaseQuery)) });
    } else {
        jQuery.ajax({
            "method": "GET",
            "url": `api/autocomplete?title=${encodeURIComponent(query)}`,
            "success": (data) => handleLookupAjaxSuccess(data, lowerCaseQuery, doneCallback, storage),
            "error": (errorData) => console.error("lookup ajax error", errorData)
        });
    }
}

function handleLookupAjaxSuccess(data, lowerCaseQuery, doneCallback, storage) {
    console.log("lookup ajax successful");
    storage.setItem(lowerCaseQuery, JSON.stringify(data));
    doneCallback({ suggestions: data });
}

function handleSelectSuggestion(suggestion) {
    console.log("Title: " + suggestion["value"] + " with ID: " + suggestion["data"]);
    window.location.href = `./single-movie.html?id=${suggestion["data"]}`;
}

function handleNormalSearch(query) {
    console.log("doing normal search with query: " + query);
    window.location.href = `./list.html?title=${encodeURIComponent(query)}&year=&director=&stars=`;
}

function bindResultsLink() {
    const resultsLink = document.querySelector('a[href="./list.html"]');
    if (resultsLink) {
        resultsLink.addEventListener('click', function (event) {
            event.preventDefault();
            const recentResultUrl = sessionStorage.getItem("recentResultUrl");
            window.location.href = recentResultUrl || "./list.html";
        });
    }
}

document.addEventListener("DOMContentLoaded", function () {
    setupAutocomplete();
    bindResultsLink();
});