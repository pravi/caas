var print_report = function (softwareName, softwareVersion) {
    var printWindow = window.open('', 'PRINT', 'height=400,width=600');
    printWindow.document.write('<html><head><title>' + document.title + '</title>');
    printWindow.document.write('<style>');
    printWindow.document.write(".passed::before { color: green; content: 'PASSED: '; }");
    printWindow.document.write(".failed::before { color: red; content: 'FAILED: '; }");
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

    $(".server_results").each(function (i, obj) {
        printWindow.document.write(obj.innerHTML);
    });

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
