<#ftl output_format="HTML">
<#macro result>
<div id="server_results">
    <#if results?size == 0>
    <h2 class="error_message">
        Results unavailable for ${domain}
    </h2>
    </#if>
    <#list results as result>
        <#if result.success>
        <div class="chip passed">
        <#else>
        <div class="chip clickable failed" onclick="showHelp('${result.getTest().short_name()}')">
        </#if>
            ${result.getTest().full_name()}
        </div>
    </#list>
</div>
</#macro>
