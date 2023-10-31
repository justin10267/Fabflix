function handleGenre_PrefixResult(resultData) {
    console.log("handleGenreResult: creating Genre list");
    let genreListBodyElement = jQuery("#genre_list");
    let prefixListBodyElement = jQuery("#prefix_list")

    for (let i = 0; i < resultData.length; i++) {
        console.log(resultData[i]["type"] + resultData[i]["value"]);
        if (resultData[i]["type"] === "Genre") {
            console.log("entered genre");
            let genreLink = jQuery("<a>").attr("href", "list.html?genre=" + resultData[i]["value"]).text(resultData[i]["value"]);
            let listItem = jQuery("<li>").append(genreLink);
            genreListBodyElement.append(listItem);
        }
        else {
            console.log("entered prefix");
            let prefix = jQuery("<a>").attr("href", "list.html?prefix=" + resultData[i]["value"]).text(resultData[i]["value"]);
            let listItem = jQuery("<li>").append(prefix);
            prefixListBodyElement.append(listItem);
        }
    }
}
document.addEventListener("DOMContentLoaded", function () {
    // Handle the "Results" link click event
    const resultsLink = document.querySelector('a[href="./list.html"]');
    resultsLink.addEventListener('click', function (event) {
        event.preventDefault(); // Prevent the default link behavior

        // Retrieve the recentResultUrl from session storage
        const recentResultUrl = sessionStorage.getItem("recentResultUrl");

        if (recentResultUrl) {
            // Navigate to the "Results" page with the recentResultUrl
            window.location.href = recentResultUrl;
        } else {
            // Fallback to the default URL if recentResultUrl is not set
            window.location.href = "./list.html";
        }
    });
});

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/main",
    success: (resultData) => handleGenre_PrefixResult(resultData)
});