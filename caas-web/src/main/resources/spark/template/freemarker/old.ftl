<#ftl output_format="HTML">
<#import "page.ftl" as page>
<#import "includes/table.ftl" as table>
<#assign scripts=["/js/table.js"] in page>
<#assign stylesheets=["/css/table.css"] in page>
<#assign description="Pick and choose your Jabber server from a list of compatible servers or check if your current server supports all required features." in page>
<#assign title="Server overview &middot; ${page.project_name}" in page>
<#assign no_results_found_msg="No results found. <a href='/add'>Add</a> credentials for some XMPP servers to get started">
<#assign footerText="&middot; Problems viewing the table? <a href='#' id='colorblind'>Colorblind mode</a> &middot; <a href='#' id='reset_table'> Compatibility view </a> " in page>
<@page.page>
    <@table.table>
    </@table.table>
</@page.page>