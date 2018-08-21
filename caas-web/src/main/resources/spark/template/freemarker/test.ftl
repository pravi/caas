<#ftl output_format="HTML">
<#import "page.ftl" as page>
<#import "includes/graph.ftl" as graph>
<#import "includes/stat.ftl" as stat>
<#assign description="Check list of servers that have implemented ${test.full_name()}, its historic implementation status" in page>
<#assign scripts=["/js/graph.js","/js/d3.min.js"] in page>
<#assign stylesheets=["/css/graph.css","/css/stat.css"] in page>
<#assign title="${test.full_name()} 's implementation across servers | ${page.project_name}" in page>

<@page.page>
    <script>
        var data = JSON.parse('${historic_data?no_esc}');
        $(function () {
            drawGraph(data, function gotoHistoric(data) {
                console.log(data);
                var url = window.location.protocol + "//" + location.hostname + ":" + location.port + "/historic/iteration/" + data.iteration;
                window.location = url;
            })
        });
    </script>
    <h2>${test.full_name()}</h2>

    <@stat.stat></@stat.stat>

    <p class="sub_heading">${test.description()}</p>

    <a href="${test.url()}" class="button">Read the specification</a>
    <br><br>

    <@graph.graph> </@graph.graph>

      <div id="server_results">
        <#list results as domain,passed>
            <div class="result chip clickable ${passed?then("passed","failed")}" onclick="location.href='/server/${domain}'">
            ${domain}
                <#if passed>
                <div class="result_image">
                    <img src="/img/passed.svg">
                </div>
                <#else>
                <div class="result_image">
                    <img src="/img/failed.svg">
                </div>
                </#if>
        </div>
        </#list>
      </div>

</@page.page>