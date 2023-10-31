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

    let moviePriceElement = document.getElementById("moviePrice");
    const moviePrice = parseFloat(resultData[0]["movie_price"]);
    if (!isNaN(moviePrice)) {
        moviePriceElement.textContent = "Price: $" + moviePrice.toFixed(2);
    } else {
        console.error("Invalid movie price: " + resultData[0]["movie_price"]);
    }

    jQuery("#addToCartButton").click(() => addToCart(movieId, resultData[0]["movie_title"], resultData[0]["movie_price"]));
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
function addToCart(movieId, movieTitle, moviePrice) {
    console.log("Adding movie to cart: " + movieTitle);
    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/cart",
        data: {
            "action": "add",
            "id": movieId,
            "title": movieTitle,
            "price": moviePrice
        },
        success: (response) => {
            alert("Added to cart!");
        },
        error: (error) => {
            alert("Failed to add to cart!");
        }
    });
}

let movieId = getParameterByName('id');
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/single-movie?id=" + movieId,
    success: (resultData) => handleResult(resultData)
});
