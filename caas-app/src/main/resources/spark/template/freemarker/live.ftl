<#ftl output_format="HTML">
<#import "page.ftl" as page>
<#assign description="Loading compliance results for ${domain}" in page>
<#assign title="${page.project_name}: Testing ${domain} for compliance" in page>
<#assign scripts=["/js/live.js"] in page>
<@page.page>
<script>
    var domain = "${domain}";
</script>
    <h2>Running tests for ${domain}</h2>
    <div id="result_error_msg"></div>
    <div id="loading_result" class="loader"></div>
</@page.page>