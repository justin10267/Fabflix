function handleConfirmationResult(resultData) {
    console.log(resultData);
    let confirmationTableBodyElement = jQuery("#confirmation_body_table");
    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + resultData[i]["sales_ids"] + "</th>"
        rowHTML += "<th>" + '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">' + resultData[i]['movie_title'] + '</a>' + "</th>"
        rowHTML += "<th>" + resultData[i]["quantity"] + "</th>";
        rowHTML += "<th>" + resultData[i]["price"] + "</th>";
        rowHTML += "<th>" + resultData[i]["total_price"] + "</th>";
        rowHTML += "</tr>";
        confirmationTableBodyElement.append(rowHTML);
    }
}
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/confirmation",
    success: (resultData) => handleConfirmationResult(resultData)
});