<#ftl output_format="HTML">
<#import "page.ftl" as page>
<#assign description="Loading compliance results for ${domain}" in page>
<#assign title="${page.project_name}: Testing ${domain} for compliance" in page>
<@page.page>
    <h2>Running tests for ${domain}</h2>
    <div id="result_error_msg"></div>
    <div id="loading_result" class="loader"></div>
    <script>
        var socket = new WebSocket("ws://" + window.location.host + "/socket/?domain=" + "${domain}");
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
    </script>
</@page.page>