function handleResult(resultData) {
    console.log(resultData);
    const tablesContainer = document.getElementById('tablesContainer');
    resultData.forEach(table => {
        const tableElem = document.createElement('table');
        const caption = tableElem.createCaption();
        caption.textContent = table.tableName;

        const headerRow = tableElem.insertRow();
        ['Attribute', 'Type'].forEach(headerText => {
            const th = document.createElement('th');
            th.textContent = headerText;
            headerRow.appendChild(th);
        });

        table.columns.forEach(column => {
            const row = tableElem.insertRow();
            const columnName = row.insertCell(0);
            const columnType = row.insertCell(1);
            columnName.textContent = column.columnName;
            columnType.textContent = column.columnType;
        });

        tablesContainer.appendChild(tableElem);
    });
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/dashboardMain",
    success: (resultData) => handleResult(resultData)
});