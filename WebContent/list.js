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
    apiUrl = `api/list?title=${title}&year=${year}&director=${director}&star=${stars}`;
}
if (url.searchParams.has('prefix')) {
    let prefix = getParameterByName('prefix');
    apiUrl = `api/list?prefix=${prefix}`;
}

console.log(apiUrl);

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: apiUrl,
    success: (resultData) => handleMovieResult(resultData)
});