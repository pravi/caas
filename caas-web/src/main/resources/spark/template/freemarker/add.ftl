<#ftl output_format="HTML">
<#import "page.ftl" as page>
<#assign description="Test an XMPP server for compliance with latest extensions and features. Once added, we will run tests periodically for the servers notifying its subscribers about changes in its results" in page>
<#assign title="Test an XMPP server for compliance | ${page.project_name}" in page>
<#assign stylesheets=["/css/add.css"] in page>
<#assign scripts=["/js/add.js"] in page>
<@page.page>
 <noscript>Enable javascript and reload this page</noscript>
 <h2>Add/update credentials for an XMPP server to test</h2>
    <form id="form_add" action="/add/" method="post">
        <div>
            <label for="jid" class="input_label_add">JID</label>
            <input name="jid" class="input_add" id="jid" type="text"/>
        </div>
        <div>
            <label for="password" class="input_label_add">Password</label>
            <input name="password" class="input_add" id="password" type="password"/>
        </div>
        <div id="input_error_msg" class="error_message"></div>
        <div id="loading_add" class="loading_status">
            <div class="loader"></div>
            <div>Verifying your credentials</div>
        </div>
        <div>
            <input type="submit" class="button" id="button_add" value="Submit"/>
            <input name="listed" id="listed" type="checkbox"/>
            <label for="listed">Include server in list?</label>
        </div>
    </form>
</@page.page>