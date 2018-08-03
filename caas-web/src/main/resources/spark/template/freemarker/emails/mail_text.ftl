<#import "unsubscribe_text.ftl" as unsubscribe>
<#macro mail>
    <#nested>
    <#if subscriber??>
        <@unsubscribe.unsubscribe>
        </@unsubscribe.unsubscribe>
    </#if>
</#macro>
