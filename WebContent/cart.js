$(document).ready(function() {
    setupEventListeners();
    loadCartItems();
    setupAutocomplete();
});

function setupEventListeners() {
    $("#proceedToPayment").click(() => window.location.href = 'payment.html');
    $(document).on('click', '.increase-btn', function() {
        modifyItemQuantity($(this).data('title'), 'increase');
    });
    $(document).on('click', '.decrease-btn', function() {
        modifyItemQuantity($(this).data('title'), 'decrease');
    });
    $(document).on('click', '.delete-btn', function() {
        deleteItem($(this).data('title'));
    });
    $("#autocompleteSearchButton").click(() => {
        handleNormalSearch($('#autocomplete').val());
    });
    bindResultsLink();
}

function bindResultsLink() {
    const resultsLink = document.querySelector('a[href="./list.html"]');
    if (resultsLink) {
        resultsLink.addEventListener('click', function (event) {
            event.preventDefault();
            const recentResultUrl = sessionStorage.getItem("recentResultUrl");
            window.location.href = recentResultUrl || "./list.html";
        });
    }
}

function loadCartItems() {
    $.ajax({
        url: 'api/cart',
        method: 'GET',
        dataType: 'json',
        success: updateCartTable
    });
}

function updateCartTable(data) {
    let total = 0;
    $("#cartTable tbody").empty();
    data.items.forEach(item => {
        total += item.price * item.quantity;
        $("#cartTable tbody").append(createCartItemRow(item));
    });
    $("#cartTable tbody").append(`<tr><td colspan="3">Total Price</td><td colspan="2">${total}</td></tr>`);
}

function createCartItemRow(item) {
    return `
        <tr>
            <td>${item.title}</td>
            <td>
                <button class="decrease-btn" data-title="${item.title}">-</button>
                ${item.quantity}
                <button class="increase-btn" data-title="${item.title}">+</button>
            </td>
            <td>${item.price}</td>
            <td>${item.price * item.quantity}</td>
            <td><button class="delete-btn" data-title="${item.title}">Delete</button></td>
        </tr>
    `;
}

function modifyItemQuantity(id, action) {
    $.ajax({
        url: 'api/cart',
        method: 'POST',
        data: { title: id, action: action },
        success: loadCartItems
    });
}

function deleteItem(id) {
    $.ajax({
        url: 'api/cart',
        method: 'POST',
        data: { title: id, action: 'delete' },
        success: loadCartItems
    });
}

function setupAutocomplete() {
    const storage = window.sessionStorage;

    $('#autocomplete').autocomplete({
        lookup: function(query, doneCallback) {
            handleLookup(query, doneCallback, storage);
        },
        onSelect: handleSelectSuggestion,
        deferRequestBy: 300,
        minChars: 3,
        triggerSelectOnValidInput: false
    });

    $('#autocomplete').keypress(function(event) {
        if (event.keyCode === 13) {
            handleNormalSearch($('#autocomplete').val());
        }
    });
}

function handleLookup(query, doneCallback, storage) {
    if (storage.getItem(query.toLowerCase())) {
        doneCallback({ suggestions: JSON.parse(storage.getItem(query.toLowerCase())) });
    } else {
        $.ajax({
            method: "GET",
            url: `api/autocomplete?title=${encodeURIComponent(query)}`,
            success: (data) => handleLookupAjaxSuccess(data, query, doneCallback, storage)
        });
    }
}

function handleLookupAjaxSuccess(data, query, doneCallback, storage) {
    storage.setItem(query.toLowerCase(), JSON.stringify(data));
    doneCallback({ suggestions: data });
}

function handleSelectSuggestion(suggestion) {
    window.location.href = `./single-movie.html?id=${suggestion["data"]}`;
}

function handleNormalSearch(query) {
    window.location.href = `./list.html?title=${encodeURIComponent(query)}&year=&director=&stars=`;
}