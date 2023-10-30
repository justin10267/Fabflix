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
    console.log("handleMovieResult: populating movie table from resultData");
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
        rowHTML += "<th>" + movieData[i]["movie_genres"].split(",").join(", ") + "</th>";
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
        rowHTML += "</tr>";

        resultTableBodyElement.append(rowHTML);
    }

    let currentPage = parseInt(resultData["pageNum"]);
    let isLastPage = resultData["isLastPage"] === true;
    console.log("Result Data: ", resultData, "isLastPage: ", isLastPage);
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

    let currentPage = sessionStorage.getItem("currentPage");
    if (currentPage === null) {
        currentPage = 1;
    } else {
        currentPage = parseInt(currentPage);
    }
    console.log(currentPage);
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
}
if (url.searchParams.has('title')) {
    let title = getParameterByName('title');
    let year = getParameterByName('year');
    let director = getParameterByName('director');
    let stars = getParameterByName('stars');
    apiUrl = `api/list?title=${title}&year=${year}&director=${director}&stars=${stars}`;
}
if (url.searchParams.has('prefix')) {
    let prefix = getParameterByName('prefix');
    apiUrl = `api/list?prefix=${prefix}`;
}
if (url.searchParams.has('limit') || url.searchParams.has('sort')) {
    let limit = getParameterByName('limit');
    let sort = getParameterByName('sort');
    apiUrl = `api/list?limit=${limit}&sort=${sort}`;
}
if (url.searchParams.has('page')) {
    let page = getParameterByName('page');
    apiUrl = `api/list?page=${page}`;
}

console.log(apiUrl);

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: apiUrl,
    success: (resultData) => handleMovieResult(resultData)
});