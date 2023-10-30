document.addEventListener("DOMContentLoaded", function() {
    // Fetch total cart value and display on page load
    fetch("/Fabflix_war/api/cart")
        .then(response => response.json())
        .then(data => {
            document.getElementById("totalPrice").innerText = "Total Price: $" + data.totalPrice.toFixed(2);
        })
        .catch(error => {
            console.error('Error fetching cart data:', error);
        });

    const paymentForm = document.getElementById("paymentForm");

    paymentForm.addEventListener("submit", function(event) {
        event.preventDefault();

        const formData = new FormData(paymentForm);

        // Define requestOptions here
        const requestOptions = {
            method: 'POST',
            body: formData
        };

        fetch('/Fabflix_war/api/placeOrder', requestOptions)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.text(); // Getting raw text in case of non-JSON response
            })
            .then(text => {
                console.log('Raw response:', text);  // Log the raw response
                try {
                    return JSON.parse(text);  // Convert the raw response to a JSON object
                } catch (error) {
                    throw new SyntaxError("Invalid JSON format");
                }
            })
            .then(data => {
                if (data.success) {
                    // Handle successful payment
                    alert("Payment Successful!");
                } else {
                    // Handle payment failure, maybe show the message from data.message
                    alert(data.message);
                }
            })
            .catch(error => {
                console.error('Error during payment:', error);
            });
    });
});