$(function () {
    $("#loading_subscribe").hide();
    $("#form_subscribe").submit(function (event) {
        event.preventDefault();
        var email = $("#email").val();
        $("#input_msg").text("");
        $("#loading_subscribe").show();
        $.post("/subscribe/", {"email": email, "domain": domain}, function (val) {
            $("#loading_subscribe").hide();
            data = JSON.parse(val);
            $("#input_msg").text(data.message);
        });
    });
});