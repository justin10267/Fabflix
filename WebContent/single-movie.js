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

    document.querySelector("h1").textContent = resultData[0]["movie_title"];
    const movieId = resultData[0]["movie_id"];
    let movieInfoElement = jQuery("#movie_info");
    movieInfoElement.empty();

    movieInfoElement.append(
        "<p>Release Year: " + resultData[0]["movie_year"] + "</p>" +
        "<p>Director: " + resultData[0]["movie_director"] + "</p>" +
        "<p>Genres: " + resultData[0]["movie_genres"] + "</p>" +
        "<p>Rating: " + resultData[0]["movie_rating"] + "</p>"
    );

    console.log("handleResult: populating star table from resultData");

    let starInfoElement = jQuery("#star_info");

    const stars = resultData[0]["movie_stars"].split(",");
    for (let i = 0; i < stars.length; i++) {
        const star_info = stars[i].split(":");
        starInfoElement.append("<p>" + '<a href="single-star.html?id=' + star_info[0] + '">' + star_info[1] + '</a>')
    }
    jQuery("#addToCartButton").click(() => addToCart(movieId, resultData[0]["movie_title"]));

}
function addToCart(movieId, movieTitle) {
    console.log("Adding movie to cart: " + movieTitle);
    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/cart",
        data: {
            "action": "add",
            "title": movieTitle
        },
        success: (response) => {
            alert("Added to cart!");
        },
        error: (error) => {
            alert("Failed to add to cart!");
        }
    });
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
