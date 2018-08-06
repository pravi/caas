<#ftl output_format="HTML">
<#import "page.ftl" as page>
<#import "includes/table.ftl" as table>
<#assign scripts=["/js/table.js"] in page>
<#assign stylesheets=["/css/table.css"] in page>
<#assign title="Check compliance status for XMPP servers | ${page.project_name}" in page>
<#assign no_results_found_msg="No results found. <a href='/add'>Add</a> credentials for some XMPP servers to get started">
<#assign footerText=" Problems viewing the table? <a href='#' id='reset_table'> Turn off sticky headers </a> " in page>
<@page.page>
    <@table.table>
    </@table.table>
</@page.page>