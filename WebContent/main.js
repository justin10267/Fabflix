function handleGenre_PrefixResult(resultData) {
    console.log("handleGenreResult: creating Genre and Prefix list");
    let genreListBodyElement = jQuery("#genre_list");
    let prefixListBodyElement = jQuery("#prefix_list");

    resultData.forEach(item => {
        console.log(item["type"] + item["value"]);
        let listItem = jQuery("<li>").append(jQuery("<a>").attr("href", `list.html?${item["type"].toLowerCase()}=${item["value"]}`).text(item["value"]));
        if (item["type"] === "Genre") {
            genreListBodyElement.append(listItem);
        } else {
            prefixListBodyElement.append(listItem);
        }
    });
}

function fetchGenrePrefixData() {
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/main",
        success: handleGenre_PrefixResult
    });
}

document.addEventListener("DOMContentLoaded", function () {
    bindResultsLink();
    fetchGenrePrefixData();
    setupAutocomplete();
});

function bindResultsLink() {
    const resultsLink = document.querySelector('a[href="./list.html"]');
    resultsLink.addEventListener('click', function (event) {
        event.preventDefault();
        const recentResultUrl = sessionStorage.getItem("recentResultUrl");
        window.location.href = recentResultUrl || "./list.html";
    });
}

const storage = window.sessionStorage;

function setupAutocomplete() {
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
        console.log(JSON.parse(storage.getItem(lowerCaseQuery)));
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
    console.log("data");
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