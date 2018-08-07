<#ftl output_format="HTML">
<#import "page.ftl" as page>
<#import "includes/table.ftl" as table>
<#assign scripts=["/js/table.js"] in page>
<#assign stylesheets=["/css/table.css"] in page>
<#assign title="Historic compliance table for test #${iteration.getIterationNumber()} | ${page.project_name}" in page>
<#assign no_results_found_msg="No results found found for this iteration">
<#assign footerText="The test ran ${timeSince} (from ${iteration.getBegin()} GMT to ${iteration.getEnd()} GMT). <a href='#' id='reset_table'>Turn off sticky headers </a> " in page>
<@page.page>
    <@table.table>
    </@table.table>
</@page.page>