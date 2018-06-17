<#ftl output_format="HTML">
<#import "page.ftl" as page>
<#import "result.ftl" as result>
<#assign description="Historic compliance result for ${domain}" in page>
<#assign title="${page.project_name} Historic compliance result for ${domain}" in page>
<@page.page>
    <h2>Test result #${iteration.getIterationNumber()} for ${domain}</h2>
    <h3>Test ran from ${iteration.getBegin()} - ${iteration.getEnd()}</h3>
    <@result.result>
    </@result.result>
</@page.page>