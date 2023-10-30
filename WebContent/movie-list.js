
function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");
    // Populate the movie table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < resultData.length; i++) {
        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">' + resultData[i]['movie_title'] + '</a>' + "</th>"
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";

        // Split genres and create hyperlinks
        const genres = resultData[i]["movie_genres"].split(",");
        rowHTML += "<th>";
        for (let j = 0; j < genres.length; j++) {
            const genre = genres[j].trim();
            rowHTML += '<a href="list.html?genre=' + encodeURIComponent(genre) + '">' + genre + '</a>';
            if (j !== genres.length - 1) {
                rowHTML += ", ";
            }
        }
        rowHTML += "</th>";

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
        movieTableBodyElement.append(rowHTML);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the MovieListServlet
});