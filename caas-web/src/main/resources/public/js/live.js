$(function() {
    var socket;
    if (location.protocol === 'https:') {
        socket = new WebSocket("wss://" + window.location.host + "/socket/?domain=" + domain);
    } else {
        socket = new WebSocket("ws://" + window.location.host + "/socket/?domain=" + domain);
    }
    socket.onerror = function() {
        $("#loading_result").hide();
        var resultPage = window.location.protocol + "//" + window.location.host + "/server/" + domain;
        $("#result_error_msg").html("Can't create socket connection for live results. Wait for tests to finish and click <a href='" + resultPage + "'>here</a> to check the results");
    };
    socket.onmessage = function (msg) {
        console.log(msg);
        data = JSON.parse(msg.data);
        $("#loading_result").hide();
        if (data.success) {
            window.location = data.redirectLink;
        } else {
            $("#result_error_msg").text(data.message);
        }
    };
});
