function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");
    // Populate the movie table
    // Find the empty table body by id "movie_table_body"
    let resultTableBodyElement = jQuery("#result_body_table");

    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < resultData.length; i++) {
        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">' + resultData[i]['movie_title'] + '</a>' + "</th>"
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_genres"].split(",").join(", ") + "</th>";
        const stars = resultData[i]["movie_stars"].split(",");
        rowHTML += "<th>"
        for (let i = 0; i < stars.length; i++) {
            const star_info = stars[i].split(":");
            rowHTML += '<a href="single-star.html?id=' + star_info[0] + '">' + star_info[1] + '</a>';
            if (i !== stars.length - 1) {
                rowHTML += ", ";
            }
        }
        rowHTML += "</th>"
        rowHTML += "<th>" + resultData[i]["movie_rating"] + "</th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        resultTableBodyElement.append(rowHTML);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/list",
    success: (resultData) => handleMovieResult(resultData)
});