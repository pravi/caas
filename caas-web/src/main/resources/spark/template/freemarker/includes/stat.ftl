<#ftl output_format="HTML">
<#macro stat>
    <div id="stats">
        <#if stats?has_content>
        <#list stats as stat,value>
            <div class="server_stat">
                <div class="stat_result">
                    ${value}
                </div>
                <div class="stat_info">
                    ${stat}
                </div>
            </div>
        </#list>
        </#if>
    </div>
</#macro>
