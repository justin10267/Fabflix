let star_form = $("#starForm");

function handleStarResult(resultDataJson) {
    const messageContainer = $("#messageContainer");
    const message = resultDataJson.message;

    messageContainer.text(message);
}

function submitStarForm(formSubmitEvent) {
    console.log("submit star form");
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/dashboardStar", {
            method: "POST",
            data: star_form.serialize(),
            success: handleStarResult
        }
    );
}

star_form.submit(submitStarForm);