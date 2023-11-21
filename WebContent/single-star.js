function getParameterByName(target) {
    let url = window.location.href;
    target = target.replace(/[\[\]]/g, "\\$&");
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    return !results ? null : !results[2] ? '' : decodeURIComponent(results[2].replace(/\+/g, " "));
}

function handleStarInfo(resultData) {
    console.log("handleStarInfo: populating star info");
    let starInfoElement = jQuery("#star_info");
    starInfoElement.append(`<p>${resultData[0]["star_name"]}</p>`);
    starInfoElement.append(`<p>Date of Birth: ${resultData[0]["star_dob"]}</p>`);
}

function handleMovieTable(resultData) {
    console.log("handleMovieTable: populating movie table");
    let movieTableBodyElement = jQuery("#movie_table_body");

    resultData.slice(0, 10).forEach(movie => {
        let rowHTML = `<tr>
            <th><a href="single-movie.html?id=${movie["movie_id"]}">${movie["movie_title"]}</a></th>
            <th>${movie["movie_year"]}</th>
            <th>${movie["movie_director"]}</th>
        </tr>`;
        movieTableBodyElement.append(rowHTML);
    });
}

function handleResult(resultData) {
    handleStarInfo(resultData);
    handleMovieTable(resultData);
}

document.addEventListener("DOMContentLoaded", function () {
    setupAutocomplete();
    bindNavigationLinks();
    fetchStarDetails();
});

function bindNavigationLinks() {
    const resultsLink = document.querySelector('a[href="./list.html"]');
    resultsLink.addEventListener('click', function (event) {
        event.preventDefault();
        const recentResultUrl = sessionStorage.getItem("recentResultUrl");
        window.location.href = recentResultUrl ? recentResultUrl : "./list.html";
    });
    document.getElementById("autocompleteSearchButton").addEventListener("click", () => {
        handleNormalSearch($('#autocomplete').val());
    });
}

function fetchStarDetails() {
    let starId = getParameterByName('id');
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: `api/single-star?id=${starId}`,
        success: handleResult
    });
}

function setupAutocomplete() {
    const storage = window.sessionStorage;
    $('#autocomplete').autocomplete({
        lookup: function (query, doneCallback) {
            handleLookup(query, doneCallback, storage)
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