<<<<<<< HEAD
window.onload = function() {
    fetchCartData();
};

function fetchCartData() {
    fetch('/api/cart', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => response.json())
        .then(data => {
            let cartDiv = document.getElementById('cartContent');
            cartDiv.innerHTML = ''; // Clear current content

            // Loop through each item in the cart and display it
            data.forEach(item => {
                cartDiv.innerHTML += `<p>${item.movie_title} - ${item.quantity}</p>`;
            });
        })
        .catch(error => {
            console.error('Error fetching cart data:', error);
        });
}
=======
window.onload = function() {
    fetchCartData();
};

function fetchCartData() {
    fetch('/api/cart', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => response.json())
        .then(data => {
            let cartDiv = document.getElementById('cartContent');
            cartDiv.innerHTML = ''; // Clear current content

            // Loop through each item in the cart and display it
            data.forEach(item => {
                cartDiv.innerHTML += `<p>${item.movie_title} - ${item.quantity}</p>`;
            });
        })
        .catch(error => {
            console.error('Error fetching cart data:', error);
        });
}
>>>>>>> 83fc3c7 (Front end modify, project 1)
