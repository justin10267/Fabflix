function handleGenreResult(resultData) {
    console.log("handleGenreResult: creating Genre list");
    let genreListBodyElement = jQuery("#genre_list");

    for (let i = 0; i < resultData.length; i++) {
        // Concatenate the html tags with resultData jsonObject
        let genreLink = jQuery("<a>").attr("href", "list.html?genre=" + resultData[i]["genre"]).text(resultData[i]["genre"]);
        let listItem = jQuery("<li>").append(genreLink);
        genreListBodyElement.append(listItem);
    }
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/main",
    success: (resultData) => handleGenreResult(resultData)
});