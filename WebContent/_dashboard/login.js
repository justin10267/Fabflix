let login_form = $("#login_form");

function handleLoginResult(resultDataJson) {
    console.log("handle login response");
    console.log(resultDataJson);
    console.log(typeof(resultDataJson))
    console.log(resultDataJson["status"]);
    if (resultDataJson["status"] === "success") {
        window.location.replace("main.html");
    } else {
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#login_error_message").text(resultDataJson["message"]);
    }
}

function submitLoginForm(formSubmitEvent) {
    console.log("submit employee login form");
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/dashboardlogin", {
            method: "POST",
            data: login_form.serialize(),
            success: handleLoginResult
        }
    );
}

login_form.submit(submitLoginForm);