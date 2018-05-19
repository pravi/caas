<#ftl output_format="HTML">
<#import "page.ftl" as page>
<#assign description="Get detailed report for your XMPP servers' compliance test results, periodic reports." in page>
<#assign title="Test your XMPP server for compliance" in page>
<@page.page>
    <style>
        #form_add * {
            font-size: 0.9em;
        }

        #form_add > div {
            margin: 5px;
            padding: 5px;
        }

        .input_label_add {
            display: inline-block;
            width: 120px;
            margin-right: 10px;
            text-align: right;
        }

        .input_add {
            width: 300px;
        }

        #button_add {
            width: 150px;
            border-radius: 101px;
        }

        .error_message {
            color: red;
            display: block;
            margin: auto;
            padding: 10px;
            text-align: center;
        }

        @media only screen and (max-width: 500px) {
            .input_add {
                width: 170px;
                font-size: 0.85em;
            }

            .input_label_add {
                width: 65px;
                font-size: 0.85em;
                margin-right: 5px;
            }

            .form_button_server_add {
                margin-left: 50px;
            }
        }
    </style>
    <h2>Add credentials for an XMPP server to test</h2>
    <form id="form_add" action="/add/" method="post">
        <div>
            <label for="jid" class="input_label_add">JID</label>
            <input name="jid" class="input_add" id="jid" type="text"/>
        </div>
        <div>
            <label for="password" class="input_label_add">Password</label>
            <input name="password" class="input_add" id="pass" type="password"/>
        </div>
        <div id="input_error_msg" class="error_message"></div>
        <div>
            <input type="submit" class="button" id="button_add" value="Submit"/>
            <input name="public" id="public" type="checkbox"/>
            <label for="public">Is this server public?</label>
        </div>
    </form>
    <script>
        $(function () {
            var jid, pass, public = false;
            var jidregex = new RegExp("(?:(?:[^@/<>'\"]+)@)(?:[^@<>'\"]+)$");
            var form = $("#form_add");
            var requestQueued = false;

            /*
            Checks if request can be sent by checking if any request already exists,
            the validity of jid and password supplied.
            Moreover, it updates the error message according to where the error
             */
            var canRequestBeSent = function (event) {
                //If we already have a request queued
                if (requestQueued) {
                    $('#input_error_msg').text("Processing your request");
                    $('#button_add').prop('disabled', true);
                    return false;
                }
                jid = $("#jid").val();
                pass = $("#pass").val();
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
            $('#pass').keyup(canRequestBeSent);
            $('#public').click(function () {
                public = !public;
            }.bind(this));

            form.submit(function (event) {
                event.preventDefault();
                if (!canRequestBeSent()) {
                    return;
                }
                $.post("/add/", {"jid": jid, "password": pass, "public": public}, function (val) {
                    data = JSON.parse(val);
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

    </script>
</@page.page>