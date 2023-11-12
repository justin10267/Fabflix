function handleResult(resultData) {
    console.log(resultData);
    const tablesContainer = document.getElementById('tablesContainer');

    resultData.forEach(table => {
        // Create a container for each table and its name
        const tableContainer = document.createElement('div');
        tableContainer.className = 'table-container';

        // Create and append the table name element
        const tableNameElem = document.createElement('h2');
        tableNameElem.className = 'table-name';
        tableNameElem.textContent = table.tableName;
        tableContainer.appendChild(tableNameElem);

        const tableElem = document.createElement('table');
        tableElem.className = 'table';

        const thead = tableElem.createTHead(); // Create thead element
        const headerRow = thead.insertRow();

        ['Attribute', 'Type'].forEach(headerText => {
            const th = document.createElement('th');
            th.textContent = headerText;
            headerRow.appendChild(th);
        });

        const tbody = document.createElement('tbody'); // Create tbody element
        tableElem.appendChild(tbody); // Append tbody to the table

        table.columns.forEach(column => {
            const row = tbody.insertRow(); // Insert row into tbody instead of table
            const columnName = row.insertCell(0);
            const columnType = row.insertCell(1);
            columnName.textContent = column.columnName;
            columnType.textContent = column.columnType;
        });

        tableContainer.appendChild(tableElem);
        tablesContainer.appendChild(tableContainer);
    });
}


jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/dashboardMain",
    success: (resultData) => handleResult(resultData)
});