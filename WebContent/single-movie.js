function getParameterByName(target) {
    let url = window.location.href;
    target = target.replace(/[\[\]]/g, "\\$&");
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)");
    let results = regex.exec(url);
    return !results ? null : !results[2] ? '' : decodeURIComponent(results[2].replace(/\+/g, " "));
}

function handleResult(resultData) {
    console.log("handleResult: populating movie info from resultData");
    document.querySelector("h1").textContent = resultData[0]["movie_title"];
    let movieInfoElement = jQuery("#movie_info").empty();
    let genreLine = resultData[0]["movie_genres"].split(",").map(genre => `<a href="./list.html?genre=${genre.trim()}">${genre.trim()}</a>`).join(", ");
    let starLine = resultData[0]["movie_stars"].split(",").map(star => {
        let [id, name] = star.split(":");
        return `<a href="single-star.html?id=${id.trim()}">${name.trim()}</a>`;
    }).join(", ");
    movieInfoElement.append(`
        <p>Release Year: ${resultData[0]["movie_year"]}</p>
        <p>Director: ${resultData[0]["movie_director"]}</p>
        <p>Genres: ${genreLine}</p>
        <p>Rating: ${resultData[0]["movie_rating"]}</p>
        <p>Stars: ${starLine}</p>
    `);
    let moviePriceElement = document.getElementById("moviePrice");
    const moviePrice = parseFloat(resultData[0]["movie_price"]);
    moviePriceElement.textContent = isNaN(moviePrice) ? "Price: N/A" : "Price: $" + moviePrice.toFixed(2);
    jQuery("#addToCartButton").click(() => addToCart(resultData[0]["movie_id"], resultData[0]["movie_title"], resultData[0]["movie_price"]));
}

let movieId = getParameterByName('id');
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/single-movie?id=" + movieId,
    success: handleResult
});

document.addEventListener("DOMContentLoaded", function () {
    setupAutocomplete();
    bindResultsLink();
});

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

function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated")
    if (storage.getItem(query.toLowerCase())) {
        console.log("using autocompleteCache for lookup")
        console.log(JSON.parse(storage.getItem(query.toLowerCase())));
        doneCallback( { suggestions: JSON.parse(storage.getItem(query.toLowerCase())) } );
    }
    else {
        console.log("sending AJAX request to backend Java Servlet")
        jQuery.ajax({
            "method": "GET",
            "url": `api/autocomplete?title=${escape(query)}`,
            "success": function(data) {
                console.log(`api/autocomplete?title=${escape(query)}`)
                handleLookupAjaxSuccess(data, query, doneCallback)
            },
            "error": function(errorData) {
                console.log("lookup ajax error")
                console.log(errorData)
            }
        })
    }
}

function handleLookupAjaxSuccess(data, query, doneCallback) {
    console.log("lookup ajax successful")
    console.log(data);
    storage.setItem(query.toLowerCase(), JSON.stringify(data));
    doneCallback( { suggestions: data } );
}

function handleSelectSuggestion(suggestion) {
    console.log("Title: " + suggestion["value"] + " with ID: " + suggestion["data"])
    window.location.href = `./single-movie.html?id=${suggestion["data"]}`;
}

function handleNormalSearch(query) {
    console.log("doing normal search with query: " + query);
    window.location.href = `./list.html?title=${query}&year=&director=&stars=`;
}

function addToCart(movieId, movieTitle, moviePrice) {
    console.log("Adding movie to cart: " + movieTitle);
    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/cart",
        data: {
            "action": "add",
            "id": movieId,
            "title": movieTitle,
            "price": moviePrice
        },
        success: (response) => {
            alert("Added to cart!");
        },
        error: (error) => {
            alert("Failed to add to cart!");
        }
    });
}