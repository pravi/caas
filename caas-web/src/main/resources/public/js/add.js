$(function () {
    var jid, pass, listedServer = false;
    var jidregex = new RegExp("(?:(?:[^@/<>'\"]+)@)(?:[^@<>'\"]+)$");
    var requestQueued = false;
    $("#loading_add").hide();

    /*
    Checks if request can be sent by checking if any request already exists,
    the validity of jid and password supplied.
    Moreover, it updates the error message according to where the error
     */
    var canRequestBeSent = function () {
        //If we already have a request queued
        if (requestQueued) {
            $('#button_add').prop('disabled', true);
            return false;
        }
        jid = $("#jid").val();
        pass = $("#password").val();
        if (!jidregex.test(jid)) {
            $('#input_error_msg').text("Invalid JID");
            $('#button_add').prop('disabled', true);
        }
        else if (pass == '') {
            $('#input_error_msg').text("Password empty");
            $('#button_add').prop('disabled', true);
        }
        else {
            $('#button_add').prop('disabled', false);
            $('#input_error_msg').text("");
        }
        return pass != '' && jidregex.test(jid);
    };

    $('#jid').keyup(canRequestBeSent);

    $('#password').keyup(canRequestBeSent);

    $('#listed').click(function () {
        listedServer = !listedServer;
    }.bind(this));

    $("#form_add").submit(function (event) {
        event.preventDefault();
        if (!canRequestBeSent()) {
            return;
        }
        $("#loading_add").show();
        $.post("/add/", {"jid": jid, "password": pass, "listed": listedServer}, function (val) {
            data = JSON.parse(val);
            $("#loading_add").hide();
            if (data.success) {
                window.location = data.redirectLink;
            } else {
                $("#input_error_msg").text(data.message);
            }
            requestQueued = false;
        }.bind(this));
        requestQueued = true;
        canRequestBeSent();
    });
});
