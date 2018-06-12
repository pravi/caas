<#ftl output_format="HTML">
<#import "page.ftl" as page>
<#import "graph.ftl" as graph>
<#import "stat.ftl" as stat>
<#assign description="Compliance result for ${test.full_name()}" in page>

<#assign title="${page.project_name}: Compliance result for ${test.full_name()}" in page>
<@page.page>
    <script>
        $(function () {
            var data = JSON.parse('${historic_data?no_esc}');
            drawGraph(data);
        });
    </script>
    <h2>${test.full_name()}</h2>
    <@stat.stat></@stat.stat>
    <p class="sub_heading">${test.description()}</p>
    <a href="${test.url()}" class="button">Read the specification</a>
    <br><br>

    <@graph.graph>
    </@graph.graph>

      <div id="server_results">
        <#list results as domain,passed>
            <#if passed>
                <div class="chip clickable passed" onclick="location.href='/server/${domain}'">
            <#else>
                <div class="chip clickable failed" onclick="location.href='/server/${domain}'">
            </#if>
            ${domain}
        </div>
        </#list>
      </div>
</@page.page>