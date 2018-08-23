var print_report = function (softwareName, softwareVersion) {
    var printWindow = window.open('', 'PRINT', 'height=400,width=600');
    printWindow.document.write('<html><head><title>' + document.title + '</title>');
    printWindow.document.write('<style>');
    printWindow.document.write("h1,h2,h3 { text-align: center; }");
    printWindow.document.write('</style>');
    printWindow.document.write('</head>');
    printWindow.document.write('<body>');
    printWindow.document.write('<h1>' + document.title + '</h1>');
    printWindow.document.write('<h2>' + 'Tests ran on ' + timestamp + '</h2>');
    if (softwareName.length > 0) {
        printWindow.document.write('<h3>' + 'Server is running ' + softwareName + ' ' + softwareVersion + '</h3>');
    } else {
        printWindow.document.write('<h3>' + 'Server is running unknown software' + '</h3>');
    }

    printWindow.document.write('<h2>' + 'Test results' + '</h2>');

    var table = "<style type='text/css'>" +
        "table {" +
            "border:solid #000 !important;" +
            "border-width:1px 0 0 1px !important;" +
        "}" +
        "th, td {" +
            "border:solid #000 !important;" +
            "border-width:0 1px 1px 0 !important;" +
        "}" +
   "</style>";
    table += "<table><thead><th>Test</th><th>Result</th></thead>";
    table += "<tbody>"
    $(".results_container").each(function (i, obj) {
        var children = [].slice.call(obj.children);
        var passText = "<p style='color: green'>PASSED</p>"
        var failText = "<p style='color: red'>FAILED</p>";
        children.forEach(function (child) {
            var passed = child.classList.contains("passed");
            table += "<tr><td>" + child.innerText + "</td><td>" + (passed ? passText : failText) + "</td></tr>";
        });
    });
    table += "</tbody></table>";

    printWindow.document.write(table);
    printWindow.print();
    printWindow.close();

    return true;
};
var shown;
var preventModalClosing = function (e) {
    e.stopPropagation(); // this stops the event from bubbling up to the doucment
};

function showHelp(name) {
    shown = $('#' + name);
    shown.css("display", "block");
    $('#help_container').css("visibility", "visible");
    $(".close").click(close_modal);
}

function close_modal() {
    if (shown) {
        shown.css("display", "none");
    }
    $('#help_container').css("visibility", "hidden");
}

$(function () {
    //Close modal on clicking anywhere
    $("#content").click(close_modal);
    //Prevent closing of modal when clicking inside help card or chip
    $(".help").click(preventModalClosing);
    $(".chip").click(preventModalClosing);
    drawGraph(data, function gotoHistoric(data) {
        var url = window.location.protocol + "//" + location.hostname + ":" + location.port + "/historic/server/" + domain + "/iteration/" + data.iteration;
        window.location = url;
    })
});
