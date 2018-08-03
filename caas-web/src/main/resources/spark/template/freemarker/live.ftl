<#ftl output_format="HTML">
<#import "page.ftl" as page>
<#assign title="Testing ${domain} for compliance | ${page.project_name}" in page>
<#assign scripts=["/js/live.js"] in page>
<@page.page>
<script>
    var domain = "${domain}";
</script>
    <noscript>Enable javascript and reload this page or visit <a href="/server/${domain}">result page and refresh after some time</a></noscript>
    <h2>Running tests for ${domain}</h2>
    <div id="result_error_msg"></div>
    <div id="loading_result" class="loader"></div>
</@page.page>