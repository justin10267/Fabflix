let genre_form = $("#genreForm");

function handleGenreResult(resultDataJson) {
    const messageContainer = $("#messageContainer");
    const message = resultDataJson.message;

    messageContainer.text(message);
}

function submitMovieForm(formSubmitEvent) {
    console.log("submit genre form");
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/dashboardGenre", {
            method: "POST",
            data: movie_form.serialize(),
            success: handleGenreResult
        }
    );
}

genre_form.submit(submitMovieForm);