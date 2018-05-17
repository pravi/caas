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
            <input serverName="jid" class="input_add" type="text"/>
        </div>
        <div>
            <label for="password" class="input_label_add">Password</label>
            <input serverName="password" class="input_add" type="password"/>
        </div>
        <div>
            <input type="submit" class="button" id="button_add" value="Submit"/>
        </div>
    </form>
</@page.page>