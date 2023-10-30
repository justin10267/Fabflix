$(document).ready(function() {
    loadCartItems();

    $("#proceedToPayment").click(function() {
        window.location.href = 'payment.html';  // assuming you'll have a payment.html for the payment process
    });

    // Binding event listeners for dynamically added buttons
    $(document).on('click', '.increase-btn', function() {
        const title = $(this).data('title');
        increaseQuantity(title);
    });

    $(document).on('click', '.decrease-btn', function() {
        const title = $(this).data('title');
        decreaseQuantity(title);
    });

    $(document).on('click', '.delete-btn', function() {
        const title = $(this).data('title');
        deleteItem(title);
    });
});

function loadCartItems() {
    $.ajax({
        url: '/Fabflix_war/api/cart',
        method: 'GET',
        dataType: 'json',
        success: function(data) {
            $("#cartTable tbody").empty();
            let total = 0;
            data.items.forEach(item => {
                total += item.price * item.quantity;
                $("#cartTable tbody").append(`
                    <tr>
                        <td>${item.title}</td>
                        <td>
                            <button class="decrease-btn" data-title="${item.title}">-</button>
                            ${item.quantity}
                            <button class="increase-btn" data-title="${item.title}">+</button>
                        </td>
                        <td>${item.price}</td>
                        <td>${item.price * item.quantity}</td>
                        <td>
                            <button class="delete-btn" data-title="${item.title}">Delete</button>
                        </td>
                    </tr>
                `);
            });
            $("#cartTable tbody").append(`
                <tr>
                    <td colspan="3">Total Price</td>
                    <td colspan="2">${total}</td>
                </tr>
            `);
        }
    });
}

function increaseQuantity(id) {
    modifyItemQuantity(id, 'increase');
}

function decreaseQuantity(id) {
    modifyItemQuantity(id, 'decrease');
}

function deleteItem(id) {
    $.ajax({
        url: '/Fabflix_war/api/cart',
        method: 'POST',
        data: {
            title: id,
            action: 'delete'
        },
        success: function() {
            loadCartItems();
        }
    });
}

function modifyItemQuantity(id, action) {
    $.ajax({
        url: '/Fabflix_war/api/cart',
        method: 'POST',
        data: `title=${id}&action=${action}`,  // modify data format
        success: function() {
            loadCartItems();
        }
    });
}
