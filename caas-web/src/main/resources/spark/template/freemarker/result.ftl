<#ftl output_format="HTML">
<#macro result>
<h3>Results</h3>
<div class="server_results">
    <#if results?size != 0>
    <#list results as result>
        <#if !result.getTest().informational()>
            <#if result.success>
        <div class="chip clickable passed" onclick="location.href='/test/${result.getTest().short_name()}'">
            <#else>
        <div class="chip clickable failed" onclick="showHelp('${result.getTest().short_name()}')">
            </#if>
            ${result.getTest().full_name()}
        </div>
        </#if>
    </#list>
</div>
<h3>Results for informational tests</h3>
<div class="server_results">
        <#list results as result>
            <#if result.getTest().informational()>
                <#if result.success>
        <div class="chip clickable passed" onclick="location.href='/test/${result.getTest().short_name()}'">
                <#else>
        <div class="chip clickable failed" onclick="showHelp('${result.getTest().short_name()}')">
                </#if>
                ${result.getTest().full_name()}
        </div>
            </#if>
        </#list>
</div>
    <#else>
    <h2 class="error_message">
        Results unavailable for ${domain}
    </h2>
    </#if>
</#macro>
