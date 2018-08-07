<#ftl output_format="HTML">
<#import "page.ftl" as page>
<#import "includes/result.ftl" as result>
<#assign title="Historic test result #${iteration.getIterationNumber()} for ${domain} | ${page.project_name}" in page>
<@page.page>
    <h2>Test result #${iteration.getIterationNumber()} for ${domain}</h2>
    <h3>Test ran ${timeSince} (from ${iteration.getBegin()} - ${iteration.getEnd()})</h3>
    <@result.result>
    </@result.result>
    <a href="/historic/iteration/${iteration.getIterationNumber()}">
        <button>
            See complete compliance table for test #${iteration.getIterationNumber()}
        </button>
    </a>
</@page.page>