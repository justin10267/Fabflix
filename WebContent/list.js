function getParameterByName(target) {
    let url = window.location.href;
    target = target.replace(/[\[\]]/g, "\\$&");

    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    return decodeURIComponent(results[2].replace(/\+/g, " "));
}
function handleMovieResult(resultData) {
    console.log(resultData);
    sessionStorage.setItem("currentPage", resultData["pageNum"]);
    sessionStorage.setItem("isLastPage", resultData["isLastPage"]);
    let resultTableBodyElement = jQuery("#result_body_table");
    let movieData = resultData["data"]

    for (let i = 0; i < movieData.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + '<a href="single-movie.html?id=' + movieData[i]['movie_id'] + '">' + movieData[i]['movie_title'] + '</a>' + "</th>"
        rowHTML += "<th>" + movieData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + movieData[i]["movie_director"] + "</th>";
        const genres = movieData[i]["movie_genres"].split(",");
        rowHTML += "<th>";
        for (let j = 0; j < genres.length; j++) {
            const genre = genres[j].trim();
            rowHTML += '<a href="list.html?genre=' + encodeURIComponent(genre) + '">' + genre + '</a>';
            if (j !== genres.length - 1) {
                rowHTML += ", ";
            }
        }
        rowHTML += "</th>";
        let stars = movieData[i]["movie_stars"].split(",");
        rowHTML += "<th>"
        for (let i = 0; i < stars.length; i++) {
            let star_info = stars[i].split(":");
            rowHTML += '<a href="single-star.html?id=' + star_info[0] + '">' + star_info[1] + '</a>';
            if (i !== stars.length - 1) {
                rowHTML += ", ";
            }
        }
        rowHTML += "</th>"
        rowHTML += "<th>" + movieData[i]["movie_rating"] + "</th>";
        rowHTML += `<th><button class="addToCart" data-movie-id="${movieData[i]['movie_id']}" data-movie-title="${movieData[i]['movie_title']}" data-movie-price="${movieData[i]['movie_price']}">Add to Cart</button></th>`;
        rowHTML += "</tr>";


        resultTableBodyElement.append(rowHTML);
    }

    let currentPage = parseInt(resultData["pageNum"]);
    let isLastPage = resultData["isLastPage"] === true;
    const nextButton = document.getElementById("nextPageButton");
    nextButton.disabled = isLastPage;
    const previousButton = document.getElementById("previousPageButton");
    previousButton.disabled = currentPage === 1;
}

document.addEventListener("DOMContentLoaded", function () {
    // Event handler for the "Update" button
    document.getElementById("updateButton").addEventListener("click", function () {
        const selectedMoviesPerPage = document.getElementById("moviesPerPage").value;
        const selectedSortOrder = document.getElementById("sortOrder").value;
        sessionStorage.setItem("preferredLimit", selectedMoviesPerPage);
        sessionStorage.setItem("preferredSort", selectedSortOrder);
        sessionStorage.setItem("currentPage", "1");
        const url = `/Fabflix_war/list.html?limit=${selectedMoviesPerPage}&sort=${selectedSortOrder}`;
        window.location.href = url;
    });

    document.body.addEventListener("click", function(event) {
        if (event.target.classList.contains("addToCart")) {
            const movieId = event.target.getAttribute("data-movie-id");
            const movieTitle = event.target.getAttribute("data-movie-title");
            const moviePrice = event.target.getAttribute("data-movie-price");
            console.log(moviePrice)
            addToCart(movieId, movieTitle, moviePrice);
        }
    });
    function addToCart(movieId, movieTitle, moviePrice) {
        console.log("Adding movie to cart: " + movieTitle);
        jQuery.ajax({
            dataType: "json",
            method: "POST",
            url: "api/cart",
            data: {
                "action": "add",
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
    let currentPage = sessionStorage.getItem("currentPage");
    if (currentPage === null) {
        currentPage = 1;
    } else {
        currentPage = parseInt(currentPage);
    }
    document.getElementById("previousPageButton").addEventListener("click", function () {
        console.log("previous clicked");
        const newPage = currentPage - 1;
        sessionStorage.setItem("currentPage", newPage.toString());
        window.location.href = `/Fabflix_war/list.html?page=${newPage}`;
    });

    document.getElementById("nextPageButton").addEventListener("click", function () {
        console.log("next clicked");
        const newPage = currentPage + 1;
        sessionStorage.setItem("currentPage", newPage.toString());
        window.location.href = `/Fabflix_war/list.html?page=${newPage}`;
    });

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
            window.location.href = "/Fabflix_war/list.html";
        }
    });
})

const userPreferredLimit = sessionStorage.getItem("preferredLimit");
const userPreferredSort = sessionStorage.getItem("preferredSort");
console.log("Preferred Limit: ", userPreferredLimit);
console.log("Preferred Sort: ", userPreferredSort);
if (userPreferredLimit) {
    const moviesPerPageDropdown = document.getElementById('moviesPerPage');
    for (let i = 0; i < moviesPerPageDropdown.options.length; i++) {
        if (moviesPerPageDropdown.options[i].value === userPreferredLimit) {
            moviesPerPageDropdown.options[i].selected = true;
            break;
        }
    }
}
if (userPreferredSort) {
    const sortOrderDropdown = document.getElementById('sortOrder');
    for (let i = 0; i < sortOrderDropdown.options.length; i++) {
        if (sortOrderDropdown.options[i].value === userPreferredSort) {
            sortOrderDropdown.options[i].selected = true;
            break;
        }
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */
url = new URL(window.location.href);

let apiUrl;
if (url.searchParams.has('genre')) {
    let genre = getParameterByName('genre');
    apiUrl = `api/list?genre=${genre}`;
    sessionStorage.setItem("recentResultUrl", `list.html?genre=${genre}`);
}
if (url.searchParams.has('title')) {
    let title = getParameterByName('title');
    let year = getParameterByName('year');
    let director = getParameterByName('director');
    let stars = getParameterByName('stars');
    apiUrl = `api/list?title=${title}&year=${year}&director=${director}&stars=${stars}`;
    sessionStorage.setItem("recentResultUrl", `list.html?title=${title}&year=${year}&director=${director}&stars=${stars}`);
}
if (url.searchParams.has('prefix')) {
    let prefix = getParameterByName('prefix');
    apiUrl = `api/list?prefix=${prefix}`;
    sessionStorage.setItem("recentResultUrl", `list.html?prefix=${prefix}`);
}
if (url.searchParams.has('limit') || url.searchParams.has('sort')) {
    let limit = getParameterByName('limit');
    let sort = getParameterByName('sort');
    apiUrl = `api/list?limit=${limit}&sort=${sort}`;
    sessionStorage.setItem("recentResultUrl", `list.html?limit=${limit}&sort=${sort}`);
}
if (url.searchParams.has('page')) {
    let page = getParameterByName('page');
    apiUrl = `api/list?page=${page}`;
    sessionStorage.setItem("recentResultUrl", `list.html?page=${page}`);
}

console.log(apiUrl);

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: apiUrl,
    success: (resultData) => handleMovieResult(resultData)
});