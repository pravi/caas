<#ftl output_format="HTML">
<#import "page.ftl" as page>
<#assign description="Get detailed report for your XMPP servers' compliance test results, periodic reports" in page>
<#assign title="${page.project_name}: Error ${error_code}" in page>
<@page.page>
<div class="card">
    <h1>Error ${error_code}</h1>
    <h3>${error_msg}</h3>
</div>
</@page.page>