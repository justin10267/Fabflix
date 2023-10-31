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
    console.log("handleResult: populating star info from resultData");
    let starInfoElement = jQuery("#star_info");
    starInfoElement.append("<p>" + resultData[0]["star_name"] + "</p>");
    starInfoElement.append("<p>Date of Birth: " + resultData[0]["star_dob"] + "</p>");

    console.log("handleResult: populating movie table from resultData");
    let movieTableBodyElement = jQuery("#movie_table_body");
    for (let i = 0; i < Math.min(10, resultData.length); i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + '<a href="single-movie.html?id=' + resultData[i]["movie_id"] + '">' + resultData[i]["movie_title"]+ '</a>' + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        rowHTML += "</tr>";
        movieTableBodyElement.append(rowHTML);
    }
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
let starId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-star?id=" + starId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});