<#ftl output_format="HTML">
<#import "page.ftl" as page>
<#import "result.ftl" as result>
<#assign title="Historic test result #${iteration.getIterationNumber()} for ${domain} | ${page.project_name}" in page>
<@page.page>
    <h2>Test result #${iteration.getIterationNumber()} for ${domain}</h2>
    <h3>Test ran from ${iteration.getBegin()} - ${iteration.getEnd()}</h3>
    <@result.result>
    </@result.result>
</@page.page>