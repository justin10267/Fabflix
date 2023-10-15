/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating movie info from resultData");
    document.getElementById("movieTitle").textContent = resultData[0]["movie_title"];
    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let movieInfoElement = jQuery("#movie_info");
    movieInfoElement.empty();

    // append two html <p> created to the h3 body, which will refresh the page
    movieInfoElement.append(
        "<p>Release Year: " + resultData[0]["movie_year"] + "</p>" +
        "<p>Director: " + resultData[0]["movie_director"] + "</p>" +
        "<p>Genres: " + resultData[0]["movie_genres"] + "</p>" +
        "<p>Rating: " + resultData[0]["movie_rating"] + "</p>" +
        "<p>Stars: " + "</p>"
    );

    console.log("handleResult: populating star table from resultData");

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let starInfoElement = jQuery("#star_info");

    const stars = resultData[0]["movie_stars"].split(",");
    for (let i = 0; i < stars.length; i++) {
        const star_info = stars[i].split(":");
        starInfoElement.append("<p>" + '<a href="single-star.html?id=' + star_info[0] + '">' + star_info[1] + '</a>')
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleMovieServlet
});