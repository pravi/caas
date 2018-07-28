<#ftl output_format="HTML">
<#import "page.ftl" as page>
<#import "graph.ftl" as graph>
<#import "stat.ftl" as stat>
<#import "result.ftl" as result>
<#assign description="Check list of features supported by ${domain}, it's historic results and subscribe to alerts about ${domain}'s results" in page>
<#assign title="${domain}'s compliance result | ${page.project_name}" in page>
<#assign stylesheets=["/css/server.css","/css/graph.css","/css/stat.css"] in page>
<#assign scripts=["/js/graph.js","/js/subscribe.js","/js/d3.min.js","/js/server.js"] in page>

<@page.page>

    <script>
        var data = JSON.parse('${historic_data?no_esc}');
        var domain = "${domain}";
        var timestamp = "${timestamp}";
    </script>

    <h2> Compliance status for ${domain}</h2>

    <@stat.stat></@stat.stat>

   <button id="download_report" onclick="print_report('${softwareName!}','${softwareVersion!}')">Download report
   </button>
    <br><br>

    <#if softwareName??>
        Server is running ${softwareName} ${softwareVersion!}
    <#else>
        Server is running unknown software
    </#if>

    <br><br>

    <@graph.graph>
    </@graph.graph>

    <@result.result>
    </@result.result>

    <div id="server_run">
        Tests last ran ${timeSince}<br>
        <button onclick="location.href='/live/${domain}'">Rerun tests</button>
    </div>

    <div id="additional_server">

        <#if mailExists>
        <div class="card" id="subscribe_server">
            <h3>Subscribe to periodic reports for this server</h3>
            <form id="form_subscribe" action="#subscribe" method="post">
                <div>
                    <label for="email" class="input_label_subscribe">E-Mail</label>
                    <input id="email" name="email" class="input_subscribe" type="text"/>
                </div>
                <div id="loading_subscribe">
                    <div class="loader"></div>
                    <div>Subscribing to results for ${domain}</div>
                </div>
                <div id="input_msg"></div>
                <div>
                    <input type="submit" class="button" id="subscribe_button" value="Subscribe"/>
                </div>
            </form>
        </div>
        </#if>

        <div class="card" id="embed_server">
            <h3>Add badge to your website</h3>
            ${badgeCode?no_esc}
            <br><br>
            <div class="code">
                ${badgeCode}
            </div>
        </div>
    </div>

    <#if helps??>
    <div id="help_container">
        <#list helps as test,help>
            <div class="card help" id="${test}">
                <a class="close" href="#${test}">&times;</a>
                <h3>For <a href="/test/${test}">${tests[test].full_name()}</a>* :</h3>
                ${help?no_esc}
                <p class="footnote">
                    Note: These instructions are valid only for this particular server,
                    because of the software running on it.
                </p>
            </div>
        </#list>
    </div>
    </#if>

</@page.page>