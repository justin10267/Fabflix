let movie_form = $("#movieForm");

function handleMovieResult(resultDataJson) {
    const messageContainer = $("#messageContainer");
    const message = resultDataJson.message;

    messageContainer.text(message);
}

function submitMovieForm(formSubmitEvent) {
    console.log("submit movie form");
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/dashboardMovie", {
            method: "POST",
            data: movie_form.serialize(),
            success: handleMovieResult
        }
    );
}

movie_form.submit(submitMovieForm);