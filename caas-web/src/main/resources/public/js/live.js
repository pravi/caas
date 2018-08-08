$(function() {
    var socket;
    if (location.protocol === 'https:') {
        socket = new WebSocket("wss://" + window.location.host + "/socket/?domain=" + domain);
    } else {
        socket = new WebSocket("ws://" + window.location.host + "/socket/?domain=" + domain);
    }
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
