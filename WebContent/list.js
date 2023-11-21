const storage = window.sessionStorage;
document.addEventListener("DOMContentLoaded", function () {
    setupEventListeners();
    initializePageSettings();
    fetchAndDisplayMovies();
});

function setupEventListeners() {
    document.getElementById("updateButton").addEventListener("click", updateSettings);
    document.getElementById("previousPageButton").addEventListener("click", () => changePage(-1));
    document.getElementById("nextPageButton").addEventListener("click", () => changePage(1));
    document.getElementById("autocompleteSearchButton").addEventListener("click", () => handleNormalSearch($('#autocomplete').val()));
    document.body.addEventListener("click", addToCartHandler);
    const resultsLink = document.querySelector('a[href="./list.html"]');
    resultsLink.addEventListener('click', handleResultsLinkClick);

    $('#autocomplete').keypress(function(event) {
        if (event.keyCode === 13) { // Enter key
            handleNormalSearch($('#autocomplete').val());
        }
    });

    $('#autocomplete').autocomplete({
        lookup: handleLookup,
        onSelect: handleSelectSuggestion,
        deferRequestBy: 300,
        minChars: 3,
        triggerSelectOnValidInput: false
    });
}

function initializePageSettings() {
    setSelectedOption("moviesPerPage", sessionStorage.getItem("preferredLimit"));
    setSelectedOption("sortOrder", sessionStorage.getItem("preferredSort"));
}

function setSelectedOption(selectId, value) {
    if (value) {
        $(`#${selectId}`).val(value);
    }
}

function fetchAndDisplayMovies() {
    const apiUrl = constructApiUrl();
    console.log(apiUrl);
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: apiUrl,
        success: function(resultData) {
            handleMovieResult(resultData);
            updateRecentResultUrl(apiUrl);
        }
    });
}

function updateRecentResultUrl(apiUrl) {
    sessionStorage.setItem("recentResultUrl", apiUrl.replace('api/list', 'list.html'));
}

function constructApiUrl() {
    const urlParams = new URLSearchParams(window.location.search);
    const queryParams = ["genre", "title", "year", "director", "stars", "prefix", "limit", "sort", "page"];
    let apiUrl = 'api/list';

    for (let param of queryParams) {
        if (urlParams.has(param)) {
            apiUrl += `${apiUrl.includes('?') ? '&' : '?'}${param}=${encodeURIComponent(urlParams.get(param))}`;
            sessionStorage.setItem("recentResultUrl", `list.html?${param}=${urlParams.get(param)}`);
        }
    }
    return apiUrl;
}

function handleMovieResult(resultData) {
    console.log(resultData);
    updateSessionStorage(resultData);
    updatePaginationButtons(resultData);

    let resultTableBodyElement = jQuery("#result_body_table").empty();
    resultData["data"].forEach(movie => {
        resultTableBodyElement.append(createMovieRowHtml(movie));
    });
}

function updateSessionStorage(resultData) {
    sessionStorage.setItem("currentPage", resultData["pageNum"]);
    sessionStorage.setItem("isLastPage", resultData["isLastPage"]);
}

function updatePaginationButtons(resultData) {
    let currentPage = parseInt(resultData["pageNum"]);
    document.getElementById("previousPageButton").disabled = currentPage === 1;
    document.getElementById("nextPageButton").disabled = resultData["isLastPage"];
}

function createMovieRowHtml(movieData) {
    const genresHtml = movieData["movie_genres"].split(",").map(genre =>
        `<a href="list.html?genre=${encodeURIComponent(genre.trim())}">${genre.trim()}</a>`
    ).join(", ");
    const starsHtml = movieData["movie_stars"] ? movieData["movie_stars"].split(",").map(star => {
        const [id, name] = star.split(":");
        return `<a href="single-star.html?id=${id.trim()}">${name.trim()}</a>`;
    }).join(", ") : "N/A";

    return `<tr>
        <th><a href="single-movie.html?id=${movieData['movie_id']}">${movieData['movie_title']}</a></th>
        <th>${movieData["movie_year"]}</th>
        <th>${movieData["movie_director"]}</th>
        <th>${genresHtml}</th>
        <th>${starsHtml}</th>
        <th>${movieData["movie_rating"] || "N/A"}</th>
        <th><button class="addToCart" data-movie-id="${movieData['movie_id']}" data-movie-title="${movieData['movie_title']}" data-movie-price="${movieData['movie_price']}">Add to Cart</button></th>
    </tr>`;
}

function changePage(offset) {
    let currentPage = parseInt(sessionStorage.getItem("currentPage") || "1");
    const newPage = currentPage + offset;
    sessionStorage.setItem("currentPage", newPage.toString());
    window.location.href = `./list.html?page=${newPage}`;
}

function updateSettings() {
    const selectedMoviesPerPage = document.getElementById("moviesPerPage").value;
    const selectedSortOrder = document.getElementById("sortOrder").value;
    sessionStorage.setItem("preferredLimit", selectedMoviesPerPage);
    sessionStorage.setItem("preferredSort", selectedSortOrder);
    sessionStorage.setItem("currentPage", "1");
    window.location.href = `./list.html?limit=${selectedMoviesPerPage}&sort=${selectedSortOrder}`;
}

function addToCartHandler(event) {
    if (event.target.classList.contains("addToCart")) {
        const movieId = event.target.getAttribute("data-movie-id");
        const movieTitle = event.target.getAttribute("data-movie-title");
        const moviePrice = event.target.getAttribute("data-movie-price");
        console.log("Adding to cart:", movieTitle);
        addToCart(movieId, movieTitle, moviePrice);
    }
}

function addToCart(movieId, movieTitle, moviePrice) {
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
        success: () => alert("Added to cart!"),
        error: () => alert("Failed to add to cart!")
    });
}

function handleResultsLinkClick(event) {
    event.preventDefault();
    const recentResultUrl = sessionStorage.getItem("recentResultUrl");
    if (recentResultUrl) {
        window.location.href = recentResultUrl;
    } else {
        window.location.href = "./list.html";
    }
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