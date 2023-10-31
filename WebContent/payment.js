let payment_form = $("#paymentForm");
$.ajax({
    url: "./api/cart",
    method: "GET",
    dataType: "json",
    success: function(data) {
        document.getElementById("totalPrice").innerText = "Total Price: $" + data.totalPrice.toFixed(2);
    },
    error: function(jqXHR, textStatus, errorThrown) {
        console.error('Error fetching cart data:', errorThrown);
    }
});

function openSuccessModal() {
    let modal = document.getElementById("successModal");
    modal.style.display = "block";
}
function closeSuccessModal() {
    let modal = document.getElementById("successModal");
    modal.style.display = "none";
}

function handlePaymentResult(resultDataJson) {
    console.log("handle payment response");
    console.log(resultDataJson);
    console.log(resultDataJson.status);

    if (resultDataJson.status === "success") {
        window.location.replace("confirmation.html");

    } else {
        console.log("show error message");
        console.log(resultDataJson.message);
        $("#payment_error_message").text(resultDataJson.message);
    }
}

function submitPaymentForm(formSubmitEvent) {
    console.log("submit payment form");
    formSubmitEvent.preventDefault();

    $.ajax({
        url: "api/placeOrder",
        method: "POST",
        data: payment_form.serialize(),
        dataType: "json",
        success: handlePaymentResult
    });
}

payment_form.submit(submitPaymentForm);