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

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/main",
    success: (resultData) => handleGenre_PrefixResult(resultData)
});