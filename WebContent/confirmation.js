function handleConfirmationResult(resultData) {
    console.log(resultData);
    let confirmationTableBodyElement = jQuery("#confirmation_body_table").empty();

    resultData.forEach(item => {
        let rowHTML = `<tr>
            <th>${item["sales_ids"]}</th>
            <th><a href="single-movie.html?id=${item['movie_id']}">${item['movie_title']}</a></th>
            <th>${item["quantity"]}</th>
            <th>${item["price"]}</th>
            <th>${item["total_price"]}</th>
        </tr>`;
        confirmationTableBodyElement.append(rowHTML);
    });
}

function fetchConfirmationDetails() {
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/confirmation",
        success: handleConfirmationResult
    });
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
        if (event.keyCode === 13) {
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

document.addEventListener("DOMContentLoaded", function () {
    setupAutocomplete();
    fetchConfirmationDetails();
    bindSearchButton();
    bindResultsLink();
});

function bindSearchButton() {
    document.getElementById("autocompleteSearchButton").addEventListener("click", () => {
        handleNormalSearch($('#autocomplete').val());
    });
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