<#ftl output_format="HTML">
<#macro result>
<div id="server_results">
    <#list results as result>
        <#if result.success>
                <div class="chip passed">
        <#else>
                <div class="chip clickable failed"
                     onclick="showHelp('${result.getTest().short_name()}')">
        </#if>
        ${result.getTest().full_name()}
        </div>
    </#list>
</div>
</#macro>
